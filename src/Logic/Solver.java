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
    private final int n; //number of level 2 box
    private final int m; //number of level 3 bins
    
    private static final int M = 2000000000; //large integer (2 billion)
    
    private Level2_Box box;
    private Level3_Bin[] bins;
    
    private int boxVolume;
    private int[] binVolumes;
    
    public Solver(Level2_Box box, int n, ArrayList<Level3_Bin> bins) throws IloException {
        this.cplex = new IloCplex();
        
        this.box = box;
        this.bins = bins.toArray(new Level3_Bin[0]);
        this.n = n;
        this.m = bins.size();
        
        this.boxVolume = box.getVolume();
        
        this.binVolumes = new int[m];
        Arrays.parallelSetAll(binVolumes, i -> this.bins[i].getVolume());
    }
    
    public void optimize() throws IloException {
        //variables
        IloIntVar[][] X = new IloIntVar[n][m]; //X_ij
        IloIntVar[] Y = cplex.boolVarArray(m); //Y_j
        
        for (int i = 0; i < n; i++) {
            X[i] = cplex.boolVarArray(m);
        }
        
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
        IloIntExpr[] binVolExpr = new IloIntExpr[m];
        IloLinearIntExpr boxVolExpr;
        
        for (int j = 0; j < m; j++) {
            boxVolExpr = cplex.linearIntExpr();
            
            for (int i = 0; i < n; i++) {
                boxVolExpr.addTerm(boxVolume, X[i][j]);
            }
            
            binVolExpr[j] = cplex.prod(Y[j], cplex.sum(binVolumes[j], cplex.prod(-1, boxVolExpr)));
        }
        
        IloIntExpr objective = cplex.sum(binVolExpr);
        cplex.addMinimize(objective);
        
        //constraints
        //Lvl 2 box spatial constraints
        for (int i = 0; i < n; i++) {
            cplex.addEq(cplex.sum(X[i]), 1);
            
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
                for (int j = 0; j < m; j++) {
                    cplex.addGe(cplex.sum(leftOf[i][k], frontOf[i][k], belowOf[i][k]), cplex.sum(cplex.sum(X[i][j], X[k][j]), -1));
                }
            }
        }
        
        //Lvl 3 Bin spatial constraints
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                        cplex.sum(bins[j].getLength(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, X[i][j])))));
                cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                        cplex.sum(bins[j].getWidth(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, X[i][j])))));
                cplex.addLe(cplex.sum(z[i], box.getHeight()), cplex.sum(bins[j].getHeight(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, X[i][j])))));
            }
        }
        
        //Lvl 3 bin selection constraints
        IloLinearIntExpr[] Xsum = new IloLinearIntExpr[m];
        for (int j = 0; j < m; j++) {
            Xsum[j] = cplex.linearIntExpr();
            for (int i = 0; i < n; i++) {
                Xsum[j].addTerm(1, X[i][j]);
            }
            cplex.addLe(Y[j], Xsum[j]);
            cplex.addLe(Xsum[j], cplex.prod(M, Y[j]));
        }
        
        //Weight constraints
        IloLinearNumExpr[] XsumWeight = new IloLinearNumExpr[m];
        for (int j = 0; j < m; j++) {
            XsumWeight[j] = cplex.linearNumExpr();
            for (int i = 0; i < n; i++) {
                XsumWeight[j].addTerm(box.getWeight(), X[i][j]);
            }
            cplex.addLe(XsumWeight[j], 30);
        }
        
        System.out.println("Solving...");
        if (cplex.solve()) {
            System.out.println("Objective: " + cplex.getObjValue());
            
            for (int i = 0; i < m; i++) {
                if (cplex.getValue(Y[i]) > 0) {
                    System.out.println("Bin size used: " + bins[i].toString());
                    System.out.println("Num of boxes: " + Math.round(cplex.getValue(Xsum[i])));
                }
            }
        } else {
            System.out.println("Solution not found.");
        }
    }
}
