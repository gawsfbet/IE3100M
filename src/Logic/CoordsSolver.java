/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Stats.CplexSolution;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author Kevin
 */
public class CoordsSolver {
    private IloCplex cplex;
    
    private int n; //total amt of level 2 box
    
    private static final double M = 100000; //large integer (100K)
    
    private Level2_Box box;
    private Level3_Bin bin;
    
    public CoordsSolver(Level2_Box box, int n, Level3_Bin bin) throws IloException {
        this.cplex = new IloCplex();
        
        this.box = box;
        this.bin = bin;
        this.n = n;
    }
    
    public CplexSolution optimize(boolean output) throws IloException {
        if (!output) {
            cplex.setOut(null);
        }
        
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
        IloIntExpr objective = cplex.sum(cplex.sum(x), cplex.sum(y));
        cplex.addMinimize(objective);
        
        //constraints
        //Lvl 2 box spatial constraints
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i == k) continue;
                cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(x[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, leftOf[i][k])))));
                cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(y[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, frontOf[i][k])))));
            }
        }
        
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                cplex.addEq(cplex.sum(leftOf[i][k], frontOf[i][k]), 1);
            }
        }
        
        //Lvl 3 Bin spatial constraints
        for (int i = 0; i < n; i++) {
            cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    bin.getLength());
            cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    bin.getWidth());
        }
    }
}
