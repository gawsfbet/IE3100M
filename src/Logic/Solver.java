/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

//testing this to push and learn how ot branch

import Model.Level2_Box;
import Model.Level3_Bin;

import ilog.concert.*; //model
import ilog.cplex.*; //algo

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Kevin-Notebook
 */
//kevin's branch
public class Solver {
    private IloCplex cplex;
    private final int n; //upper bound of level 2 box
    //private final int m; //number of level 3 bin
    
    private static final int M = 2000000000; //large integer (2 billion)
    
    private Level2_Box box;
    private Level3_Bin bin;
    
    private int boxVolume;
    private int binVolumes;
    
    public Solver(Level2_Box box, int n, Level3_Bin bin) throws IloException {
        this.cplex = new IloCplex();
        
        this.box = box;
        this.bin = bin;
        this.n = n;
        //this.m = bin.size();
        
        this.boxVolume = box.getVolume();
        this.binVolumes = bin.getVolume();
    }
    
    public void optimize() throws IloException {
        //variables
        IloIntVar[] P = cplex.boolVarArray(n);
        
        //coordinates
        IloIntVar[] x = cplex.intVarArray(n, 0, Integer.MAX_VALUE); //x_i
        IloIntVar[] y = cplex.intVarArray(n, 0, Integer.MAX_VALUE); //y_i
        IloIntVar[] z = cplex.intVarArray(n, 0, Integer.MAX_VALUE); //z_i
        
        //orientation
        IloIntVar[][] leftOf = new IloIntVar[n][n]; //a_ik
        IloIntVar[][] frontOf = new IloIntVar[n][n]; //c_ik
        IloIntVar[][] belowOf = new IloIntVar[n][n]; //e_ik
        
        for (int i = 0; i < n; i++) {
            leftOf[i] = cplex.boolVarArray(n);
            frontOf[i] = cplex.boolVarArray(n);
            belowOf[i] = cplex.boolVarArray(n);
        }
        
        //alignment
        IloIntVar[] isHorizontal = cplex.boolVarArray(n); //l_xi
        
        //expressions and objective function
        /*IloLinearIntExpr boxVolExpr = cplex.linearIntExpr();

        for (int i = 0; i < n; i++) {
            boxVolExpr.addTerm(boxVolume, P[i]);
        }*/

        IloIntExpr objective = cplex.sum(P);
        cplex.addMaximize(objective);
        
        //constraints
        //Lvl 2 box spatial constraints
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i == k) continue;
                cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(x[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, leftOf[i][k])))));
                cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(y[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, frontOf[i][k])))));
                cplex.addLe(cplex.sum(z[i], box.getHeight()), cplex.sum(z[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, belowOf[i][k])))));
            }
        }
        
        //Comparing box constraints
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                cplex.addGe(cplex.sum(leftOf[i][k], frontOf[i][k], belowOf[i][k]), cplex.sum(cplex.sum(P[i], P[k]), -1));
                cplex.addLe(cplex.sum(leftOf[i][k], frontOf[i][k], belowOf[i][k]), P[i]);
                cplex.addLe(cplex.sum(leftOf[i][k], frontOf[i][k], belowOf[i][k]), P[k]);
                //cplex.addEq(cplex.sum(leftOf[i][k], frontOf[i][k], belowOf[i][k]), cplex.prod(P[i], P[k]));
            }
        }
        
        cplex.addLe(cplex.sum(P), n);
        
        //Lvl 3 Bin spatial constraints
        for (int i = 0; i < n; i++) {
            cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    cplex.sum(bin.getLength(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i])))));
            cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    cplex.sum(bin.getWidth(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i])))));
            cplex.addLe(cplex.sum(z[i], box.getHeight()), cplex.sum(bin.getHeight(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i])))));
        }
        
        //Lvl 3 bin selection constraints
        /*IloLinearIntExpr[] Xsum = new IloLinearIntExpr[m];
        for (int j = 0; j < m; j++) {
            Xsum[j] = cplex.linearIntExpr();
            for (int i = 0; i < n; i++) {
                Xsum[j].addTerm(1, P[i][j]);
            }
            cplex.addLe(Y[j], Xsum[j]);
            cplex.addLe(Xsum[j], cplex.prod(M, Y[j]));
        }*/
        
        //Weight constraints
        IloLinearNumExpr XsumWeight = cplex.linearNumExpr();
        for (int i = 0; i < n; i++) {
            XsumWeight.addTerm(box.getWeight(), P[i]);
        }
        cplex.addLe(XsumWeight, 30);
        
        System.out.println("Solving...");
        if (cplex.solve()) {
            System.out.println("Free Space: " + (bin.getVolume() - box.getVolume() * cplex.getObjValue()));
            System.out.println("Number of boxes: " + cplex.getObjValue());
            
            for (int i = 0; i < n; i++) {
                if (cplex.getValue(P[i]) > 0) {
                    System.out.println(String.format("(%d, %d, %d)", Math.round(cplex.getValue(x[i])), Math.round(cplex.getValue(y[i])), Math.round(cplex.getValue(z[i]))));
                }
            }
        } else {
            System.out.println("Solution not found.");
        }
    }
}
