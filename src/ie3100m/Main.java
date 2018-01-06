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

    public static final int numOrderedBox = 1000;

    public static void main(String[] args) {
        Level2_Box box = new Level2_Box(190, 186, 23, 0.65); //in mm and g
        //Level3_Bin bin = new Level3_Bin(570, 400, 260);
        int boxHeight = box.getHeight();
        //int binHeight = bin.getHeight();
        ArrayList<Level3_Bin> candidateBins = new ArrayList<>();
        addBins(candidateBins);

        candidateBins.forEach((bin) -> {
            try {
                chkMaxBox(box, bin);
                chkReqBinNum(box, bin, numOrderedBox);
            } catch (IloException ex) {
                ex.printStackTrace();
            }
        });

        //Bin Comparison
        Level3_Bin finalBin = candidateBins.get(0);
        int binChkr = finalBin.getNumOfBin();
        double volChkr = finalBin.getEmptyVol();
        
        /*
        1. chk the smallest empty vol. 
        2. chk the least amt of req bins.
        3. special: if amt of req bins == 1, finalBin must always have only 1 req bin, chk for smallest empty vol. 
        */
        for (int i = 0; i < candidateBins.size(); i++) {
            if (candidateBins.get(i).getNumOfBin() < binChkr) { //if number of required number of bins for bin i is lesser than that of the proposed final bin
                setFinalBin(candidateBins, finalBin, binChkr, volChkr, i);
            } else if (candidateBins.get(i).getNumOfBin() == binChkr) { //same req num of bins, chk which has lesser empty space.
                if (candidateBins.get(i).getEmptyVol() < volChkr) {
                    setFinalBin(candidateBins, finalBin, binChkr, volChkr, i);
                }
            }
        }
    }
    
    //add bins to arraylist
    public static void addBins(ArrayList<Level3_Bin> candidateBins) {
        for (int i = 0; i < 1; i++) {
            candidateBins.add(new Level3_Bin(570, 440, 190));
            candidateBins.add(new Level3_Bin(440, 440, 190));
            candidateBins.add(new Level3_Bin(390, 380, 200));
            candidateBins.add(new Level3_Bin(390, 380, 380));
        }
    }
    
    //set bin as final bin
    public static void setFinalBin(ArrayList<Level3_Bin> candidateBins, Level3_Bin finalBin, int BinChkr, double VolChkr, int i) {
        finalBin = candidateBins.get(i);
        BinChkr = finalBin.getNumOfBin();
        VolChkr = finalBin.getEmptyVol();
    }
    
    //find maximum number of boxes that can be placed in the bin.
    public static void chkMaxBox(Level2_Box box, Level3_Bin bin) throws IloException {
        System.out.println("Upper bound: " + calcUpperBound(box, bin));
        Solver solver = new Solver(box, calcUpperBound(box, bin), bin);
        int numBoxPerLvl = solver.optimize();
        int numHeight = bin.getHeight() / box.getHeight();
        int totalBoxNum = numBoxPerLvl * numHeight;
        double emptyVol = (bin.getVolume()) - (totalBoxNum * box.getVolume());
        bin.setMaxBoxNum(totalBoxNum);
        bin.setEmptyVol(emptyVol);
        System.out.println("Number of boxes for bin type " + bin.getBinIdentifier() + ": " + totalBoxNum);
        System.out.println("Empty space for bin type " + bin.getBinIdentifier() + ": " + emptyVol);
    }
    
    //find required number of bins for the requested number of boxes.
    public static void chkReqBinNum(Level2_Box box, Level3_Bin bin, int numOrderedBox) {
        //if there is less boxes requested than the maximum number of boxes the bin can hold.
        if (numOrderedBox < bin.getMaxBoxNum()) { 
            bin.setEmptyVol(bin.getVolume() - (numOrderedBox * box.getVolume()));
            bin.setNumOfBin(1);
            bin.setRemBox(0);
        } else {
            bin.setNumOfBin(numOrderedBox / bin.getMaxBoxNum());
            bin.setRemBox(numOrderedBox % bin.getMaxBoxNum());
        }
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }

}
