/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

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
public class Solver {
    private IloCplex cplex;
    private final int n; //number of level 2 boxes
    private final int m; //number of level 3 bins
    
    private static final int M = 2000000000; //large integer (2 billion)
    
    private Level2_Box[] boxes;
    private Level3_Bin[] bins;
    
    private int[] boxVolumes;
    private int[] binVolumes;
    
    public Solver(ArrayList<Level2_Box> boxes, ArrayList<Level3_Bin> bins) throws IloException {
        this.cplex = new IloCplex();
        
        this.boxes = boxes.toArray(new Level2_Box[0]);
        this.bins = bins.toArray(new Level3_Bin[0]);
        this.n = boxes.size();
        this.m = bins.size();
        
        this.boxVolumes = new int[n];
        this.binVolumes = new int[m];
        
        Arrays.parallelSetAll(boxVolumes, i -> this.boxes[i].getVolume());
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
                boxVolExpr.addTerm(boxVolumes[i], X[i][j]);
            }
            
            binVolExpr[j] = cplex.prod(Y[j], cplex.sum(binVolumes[j], cplex.prod(-1, boxVolExpr)));
        }
        
        IloIntExpr objective = cplex.sum(binVolExpr);
        cplex.addMinimize(objective);
        
        //constraints
        for (int i = 0; i < n; i++) {
            cplex.addEq(cplex.sum(X[i]), 1);
            
            for (int k = 0; k < n; k++) {
                if (i == k) continue;
                cplex.addLe(cplex.sum(x[i], cplex.prod(boxes[i].getLength(), isHorizontal[i]), cplex.prod(boxes[i].getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(x[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, leftOf[i][k])))));
                cplex.addLe(cplex.sum(y[i], cplex.prod(boxes[i].getWidth(), isHorizontal[i]), cplex.prod(boxes[i].getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(y[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, frontOf[i][k])))));
                cplex.addLe(cplex.sum(z[i], boxes[i].getHeight()), cplex.sum(z[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, belowOf[i][k])))));
            }
        }
        
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < m; j++) {
                    cplex.addGe(cplex.sum(leftOf[i][k], frontOf[i][k], belowOf[i][k]), cplex.sum(cplex.sum(X[i][j], X[k][j]), -1));
                }
            }
        }
        
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                cplex.addLe(cplex.sum(x[i], cplex.prod(boxes[i].getLength(), isHorizontal[i]), cplex.prod(boxes[i].getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                        cplex.sum(bins[j].getLength(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, X[i][j])))));
                cplex.addLe(cplex.sum(y[i], cplex.prod(boxes[i].getWidth(), isHorizontal[i]), cplex.prod(boxes[i].getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                        cplex.sum(bins[j].getWidth(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, X[i][j])))));
                cplex.addLe(cplex.sum(z[i], boxes[i].getHeight()), cplex.sum(bins[j].getHeight(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, X[i][j])))));
            }
        }
    }
}
