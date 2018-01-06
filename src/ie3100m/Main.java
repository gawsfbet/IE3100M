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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin-Notebook
 */
public class Main {
    public static void main(String[] args) {
        Level2_Box box = new Level2_Box(190, 186, 23, 0.65); //in mm and g
        //Level3_Bin bin = new Level3_Bin(570, 400, 260);
        int boxHeight = box.getHeight();
        //int binHeight = bin.getHeight();
        ArrayList<Level3_Bin> candidateBins = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            candidateBins.add(new Level3_Bin(570, 440, 190));
            candidateBins.add(new Level3_Bin(440, 440, 190));
            candidateBins.add(new Level3_Bin(390, 380, 200));
            candidateBins.add(new Level3_Bin(390, 380, 380));
        }
        
        
        candidateBins.forEach((bin)->{
            try {
                chkEmptyVol(box, bin);
            } catch (IloException ex) {
                ex.printStackTrace();
            }
        });
        
        candidateBins.forEach((bin)->{
            System.out.println("Number of boxes for bin type " + bin.getBinIdentifier() + ": " + bin.getBoxNum());
            System.out.println("Empty space for bin type " + bin.getBinIdentifier() + ": " + bin.getEmptyVol());
        });
        
//            System.out.println("Upper bound: " + calcUpperBound(box, bin));
//            Solver solver = new Solver(box, calcUpperBound(box, bin), bin);
//            int numBoxPerLvl = solver.optimize();
//            int numHeight = binHeight/boxHeight;
//            int totalBoxNum = numBoxPerLvl * numHeight;
//            double emptySpace = (bin.getVolume()) - (totalBoxNum * box.getVolume());
//            System.out.println("Number of boxes for bin type " + bin.getLength() + "x" + bin.getWidth() + "x" + bin.getHeight() + ": " + totalBoxNum);
//            System.out.println("Empty space for bin type " + bin.getLength() + "x" + bin.getWidth() + "x" + bin.getHeight() + ": " + emptySpace);
    }
    
     public static void chkEmptyVol(Level2_Box box, Level3_Bin bin) throws IloException{
        System.out.println("Upper bound: " + calcUpperBound(box, bin));
            Solver solver = new Solver(box, calcUpperBound(box, bin), bin);
            int numBoxPerLvl = solver.optimize();
            int numHeight = bin.getHeight()/box.getHeight();
            int totalBoxNum = numBoxPerLvl * numHeight;
            double emptyVol = (bin.getVolume()) - (totalBoxNum * box.getVolume());
            bin.setBoxNum(totalBoxNum);
            bin.setEmptyVol(emptyVol);
            System.out.println("Number of boxes for bin type " + bin.getBinIdentifier() + ": " + totalBoxNum);
            System.out.println("Empty space for bin type " + bin.getBinIdentifier() + ": " + emptyVol);
    }
     
    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
    
   
}
