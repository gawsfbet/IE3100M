/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Logic.Solver;
import Model.Level2_Box;
import Model.Level3_Bin;
import ilog.concert.IloException;
import java.util.ArrayList;

/**
 *
 * @author Kevin-Notebook
 */
public class Main {
    public static void main(String[] args) {
        try {
            Level2_Box box = new Level2_Box(190, 190, 100, 0.65); //in mm and g
            Level3_Bin bin = new Level3_Bin(570, 400, 260);
            //Level3_Bin bin = new Level3_Bin(570, 400, 260);
            
            /*ArrayList<Level3_Bin> candidateBins = new ArrayList<>();
            
            for (int i = 0; i < 2; i++) {
                candidateBins.add(new Level3_Bin(440, 440, 190));
                candidateBins.add(new Level3_Bin(390, 380, 200));
                candidateBins.add(new Level3_Bin(390, 380, 380));
            }*/
            
            System.out.println("Upper bound: " + calcUpperBound(box, bin));
            Solver solver = new Solver(box, calcUpperBound(box, bin), bin);
            solver.optimize();
        } catch (IloException ex) {
            ex.printStackTrace();
        }
    }
    
    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
