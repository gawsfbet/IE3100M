/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

//testing this to push and learn how ot branch

import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Stats.BoxArrangement;
import Model.Stats.CplexSolution;

import ilog.concert.*; //model
import ilog.cplex.*; //algo
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin-Notebook
 */
//kevin's branch
public class QuantitySolver {
    private IloCplex cplex;
    private int n; //upper bound of level 2 box
    
    private static final double M = 100000; //large integer (100K)
    
    private Level2_Box box;
    private Level3_Bin bin;
    
    private int boxVolume;
    private int binVolumes;
    
    public QuantitySolver(Level2_Box box, int n, Level3_Bin bin) throws IloException {
        this.cplex = new IloCplex();
        
        this.box = box;
        this.bin = bin;
        this.n = n;
        
        this.boxVolume = box.getVolume();
        this.binVolumes = bin.getVolume();
    }
    
    public int optimize(boolean output) throws IloException {
        if (!output) {
            cplex.setOut(null);
        }
        
        //variables
        IloIntVar[] P = cplex.boolVarArray(n); //whether it is in the box or not
        
        //coordinates
        IloIntVar[] x = cplex.intVarArray(n, 0, Integer.MAX_VALUE); //x_i
        IloIntVar[] y = cplex.intVarArray(n, 0, Integer.MAX_VALUE); //y_i
        
        //orientation
        IloIntVar[][] leftOf = new IloIntVar[n][n]; //a_ik
        IloIntVar[][] frontOf = new IloIntVar[n][n]; //c_ik
        
        for (int i = 0; i < n; i++) {
            leftOf[i] = cplex.boolVarArray(n);
            frontOf[i] = cplex.boolVarArray(n);
        }
        
        //alignment
        IloIntVar[] isHorizontal = cplex.boolVarArray(n); //l_xi

        //define objective
        IloIntExpr objective = cplex.sum(P);
        cplex.addMaximize(objective);
        
        //constraints
        //Lvl 2 box spatial constraints
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i == k) continue;
                cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(x[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, leftOf[i][k]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[k])))));
                cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(y[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, frontOf[i][k]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[k])))));
            }
        }
        
        //Comparing box constraints
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                cplex.addGe(cplex.sum(leftOf[i][k], frontOf[i][k]), cplex.sum(cplex.sum(P[i], P[k]), -1));
                cplex.addLe(cplex.sum(leftOf[i][k], frontOf[i][k]), P[i]);
                cplex.addLe(cplex.sum(leftOf[i][k], frontOf[i][k]), P[k]);
            }
        }
        
        cplex.addLe(cplex.sum(P), n);
        
        //Lvl 3 Bin spatial constraints
        for (int i = 0; i < n; i++) {
            cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    cplex.sum(bin.getLength(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i])))));
            cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    cplex.sum(bin.getWidth(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i])))));
        }
        
        //Weight constraints
        IloLinearNumExpr XsumWeight = cplex.linearNumExpr();
        for (int i = 0; i < n; i++) {
            XsumWeight.addTerm(box.getWeight(), P[i]);
        }
        cplex.addLe(XsumWeight, 30);
        
        if (cplex.solve()) {
            if (output) {
                System.out.println("Free Space: " + (bin.getVolume() - box.getVolume() * cplex.getObjValue()));
                System.out.println("Number of boxes: " + cplex.getObjValue());

                for (int i = 0; i < n; i++) {
                    if (cplex.getValue(P[i]) > 0) {
                        System.out.println(String.format("(%d, %d)", Math.round(cplex.getValue(x[i])), Math.round(cplex.getValue(y[i]))));
                    }
                }
            }
            
            return (int) Math.round(cplex.getObjValue());
        } else {
            System.out.println("Solution not found.");
            return Integer.MAX_VALUE;
        }
    }
    
    private BoxArrangement[] getArrangement(IloIntVar[] P, IloIntVar[] x, IloIntVar[] y, IloIntVar[] isHorizontal) throws IloException {
        ArrayList<BoxArrangement> boxArrangements = new ArrayList<>();
            
        int[] solsP = Arrays.stream(cplex.getValues(P)).mapToInt(d -> (int) Math.round(d)).toArray();
        double[] solsX = cplex.getValues(x), solsY = cplex.getValues(y), solsIsHorizontal = cplex.getValues(isHorizontal);
        
        for (int i = 0; i < solsP.length; i++) {
            if (solsP[i] == 0) {
                continue;
            }
            
            boxArrangements.add(new BoxArrangement(
                    (int) Math.round(solsX[i]), 
                    (int) Math.round(solsY[i]), 
                    Math.round(solsIsHorizontal[i]) == 1));
        }
        
        return boxArrangements.toArray(new BoxArrangement[0]);
    }
}
