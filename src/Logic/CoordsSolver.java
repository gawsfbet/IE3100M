/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Stats.BoxArrangement;
import Model.Stats.CplexSolution;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Kevin
 */
public class CoordsSolver {
    private IloCplex cplex;
    
    private final int n; //total amt of level 2 box
    
    private static final double M = 100000; //large integer (100K)
    
    private Level2_Box box;
    private Level3_Bin bin;
    
    private final int buffer;
    
    public CoordsSolver(Level2_Box box, int n, Level3_Bin bin, int buffer) throws IloException {
        this.cplex = new IloCplex();
        
        this.box = box;
        this.bin = bin;
        this.n = n;
        this.buffer = buffer;
    }
    
    public BoxArrangement[] align(boolean output) throws IloException {
        if (!output) {
            cplex.setOut(null);
        }
        
        //coordinates
        IloIntVar[] x = cplex.intVarArray(n, buffer, Integer.MAX_VALUE); //x_i
        IloIntVar[] y = cplex.intVarArray(n, buffer, Integer.MAX_VALUE); //y_i
        
        //orientation
        IloIntVar[][] leftOf = new IloIntVar[n][n]; //a_ik
        IloIntVar[][] frontOf = new IloIntVar[n][n]; //c_ik
        
        for (int i = 0; i < n; i++) {
            leftOf[i] = cplex.boolVarArray(n);
            frontOf[i] = cplex.boolVarArray(n);
        }
        
        //alignment
        IloIntVar[] isHorizontal = cplex.boolVarArray(n); //l_xi
        IloIntVar alignmentFlag = cplex.boolVar();

        //define objective
        IloNumExpr objective = cplex.sum(alignmentFlag, cplex.prod(2.0 / Math.max(bin.getLength(), bin.getWidth()), cplex.sum(cplex.sum(x), cplex.sum(y))));
        cplex.addMinimize(objective);
        
        //constraints
        //Alignment Flags
        IloNumExpr eqn = cplex.prod(-4.0 / (n * n), cplex.prod(cplex.sum(isHorizontal), cplex.sum(cplex.sum(isHorizontal), -n)));
        cplex.addGe(alignmentFlag, eqn);
        
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
                    bin.getLength() - buffer);
            cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    bin.getWidth() - buffer);
        }
        
        if (cplex.solve()) {
            if (output) {
                for (int i = 0; i < n; i++) {
                    System.out.println("Product Box Coordinates:");
                    System.out.println(String.format("(%d, %d)", Math.round(cplex.getValue(x[i])), Math.round(cplex.getValue(y[i]))));
                }
            }
            
            return getArrangement(x, y, isHorizontal);
        } else {
            System.out.println("Cannot determine box coordinates.");
            return new BoxArrangement[0];
        }
    }
    
    private BoxArrangement[] getArrangement(IloIntVar[] x, IloIntVar[] y, IloIntVar[] isHorizontal) throws IloException {
        ArrayList<BoxArrangement> boxArrangements = new ArrayList<>();
            
        double[] solsX = cplex.getValues(x), solsY = cplex.getValues(y), solsIsHorizontal = cplex.getValues(isHorizontal);
        
        return IntStream.range(0, n).mapToObj(i -> new BoxArrangement(
                (int) Math.round(solsX[i]),
                (int) Math.round(solsY[i]),
                Math.round(solsIsHorizontal[i]) == 1)).toArray(BoxArrangement[]::new);
    }
}
