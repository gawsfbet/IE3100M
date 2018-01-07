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

        printAllBinStats(candidateBins, box, numOrderedBox);

        //Bin Comparison this shit is damn messy.
        Level3_Bin finalBin = candidateBins.get(0);
        int binChkr = finalBin.getNumOfBin();
        double totalVolChkr = finalBin.getTotalEmptyVol();

//        /*
//        1. chk the smallest empty vol. 
//        2. chk the least amt of req bins.
//        3. special: if amt of req bins == 1, finalBin must always have only 1 req bin, chk for smallest empty vol. 
//         */
//        for (int i = 1; i < candidateBins.size(); i++) {
//            binChkr = finalBin.getNumOfBin();
//            totalVolChkr = finalBin.getTotalEmptyVol();
//            //ensure scenario of 1 bin 1 box does not occur
//            if (candidateBins.get(i).getNumOfBin() / numOrderedBox <= 1 / 2) {
//                //chk for remainder boxes. No remainder boxes > have remainder boxes.
//                if (finalBin.getRemBox() != 0 && candidateBins.get(i).getRemBox() == 0 /*&& candidateBins.get(i).getTotalEmptyVol() < finalBin.getTotalEmptyVol()*/) {
//                    finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
//                } else if (finalBin.getRemBox() == 0 && candidateBins.get(i).getRemBox() == 0) {
//                    //chk for smallest total empty vol (sum of all bins). 
//                    if (candidateBins.get(i).getTotalEmptyVol() < totalVolChkr) {
//                        finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
//                    } else if (candidateBins.get(i).getTotalEmptyVol() == totalVolChkr) {
//                        //chk for least no. of bins used. 
//                        if (candidateBins.get(i).getNumOfBin() < binChkr) {
//                            finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
//                        }
//                    }
//                } else if (finalBin.getRemBox() != 0 && candidateBins.get(i).getRemBox() != 0) {
//                    //chk for smallest total empty vol (sum of all bins). 
//                    if (candidateBins.get(i).getTotalEmptyVol() < totalVolChkr) {
//                        finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
//                    } else if (candidateBins.get(i).getTotalEmptyVol() == totalVolChkr) {
//                        //chk for least no. of bins used. 
//                        if (candidateBins.get(i).getNumOfBin() < binChkr) {
//                            finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
//
//                        }
//                    }
//                }
//            }
//        }
        
        for (int i = 0; i < candidateBins.size(); i++) {
            binChkr = finalBin.getNumOfBin();
            totalVolChkr = finalBin.getTotalEmptyVol();
            //ensure scenario of 1 bin 1 box does not occur
            if (candidateBins.get(i).getNumOfBin() / numOrderedBox <= 1 / 2) {
                if (candidateBins.get(i).getTotalEmptyVol() < finalBin.getTotalEmptyVol()){
                    finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
                } else if (candidateBins.get(i).getTotalEmptyVol() == finalBin.getTotalEmptyVol()) {
                    if (candidateBins.get(i).getNumOfBin() < finalBin.getNumOfBin()){
                        finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
                    } else if (candidateBins.get(i).getNumOfBin() == finalBin.getNumOfBin()){
                        if (candidateBins.get(i).getRemBox() == 0 && finalBin.getRemBox() != 0){
                            finalBin = setFinalBin(candidateBins, finalBin, binChkr, totalVolChkr, i);
                        } else if (candidateBins.get(i).getRemBox() != 0 && finalBin.getRemBox() != 0){
                            /*chk remainder box here? */
                        }
                    }
                }
            }
            
        }

        
        int remainderBox = finalBin.getRemBox();
        ArrayList<Level3_Bin> candidateFinalBins = new ArrayList<>();
        addBins(candidateFinalBins);
        candidateFinalBins.forEach((bin) -> {
            if (bin.getMaxBoxNum() > remainderBox) {
                bin.setNumOfBin(1);
                bin.setEmptyVol(bin.getVolume() - (remainderBox * box.getVolume()));
            } else {
                bin.setNumOfBin(0);
            }
        });

        Level3_Bin lastBin = candidateBins.get(0);
        for (int i = 0; i < candidateBins.size(); i++) {
            if (candidateBins.get(i).getNumOfBin() == 1) {
                if (candidateBins.get(i).getEmptyVol() < lastBin.getEmptyVol()) {
                    lastBin = candidateBins.get(i);
                }
            }
        }

        printFinalBinStats(finalBin, box, numOrderedBox);
        printLastBinStats(lastBin, box, remainderBox);

    }

    private static void printLastBinStats(Level3_Bin bin, Level2_Box box, int remainderBox) {
        System.out.println("Last Bin:");
        System.out.println("box type: " + box.getBoxIdentifier() + " remainder size: " + remainderBox);
        System.out.print("bin type " + bin.getBinIdentifier());
        System.out.print("-> boxes per bin: " + bin.getMaxBoxNum());
        System.out.print(", bins required: " + bin.getNumOfBin());
        System.out.print(", total empty vol: " + bin.getTotalEmptyVol());
        System.out.print(", empty vol per bin: " + bin.getEmptyVol());
        System.out.print(", vol inefficiency: " + (bin.getEmptyVol() / bin.getVolume()));
        System.out.println("");
    }

    private static void printFinalBinStats(Level3_Bin bin, Level2_Box box, int numOrderedBox) {
        System.out.println("Final Main Bin:");
        System.out.println("box type: " + box.getBoxIdentifier() + " order size: " + numOrderedBox);
        System.out.print("bin type " + bin.getBinIdentifier());
        System.out.print("-> boxes per bin: " + bin.getMaxBoxNum());
        System.out.print(", bins required: " + bin.getNumOfBin());
        System.out.print(", total empty vol: " + bin.getTotalEmptyVol());
        System.out.print(", empty vol per bin: " + bin.getEmptyVol());
        System.out.print(", vol inefficiency: " + (bin.getEmptyVol() / bin.getVolume()));
        System.out.print(", remainder boxes: " + bin.getRemBox());
        System.out.println("");

    }

    public static void printAllBinStats(ArrayList<Level3_Bin> candidateBins, Level2_Box box, int numOrderedBox) {
        System.out.println("box type: " + box.getBoxIdentifier() + " order size: " + numOrderedBox);
        for (Level3_Bin bin : candidateBins) {
            System.out.print("bin type " + bin.getBinIdentifier());
            System.out.print("-> boxes per bin: " + bin.getMaxBoxNum());
            System.out.print(", bins required: " + bin.getNumOfBin());
            System.out.print(", total empty vol: " + bin.getTotalEmptyVol());
            System.out.print(", empty vol per bin: " + bin.getEmptyVol());
            System.out.print(", vol inefficiency: " + (bin.getEmptyVol() / bin.getVolume()));
            System.out.print(", remainder boxes: " + bin.getRemBox());
            System.out.println("");
        }
    }

    //add bins to arraylist
    public static void addBins(ArrayList<Level3_Bin> candidateBins) {
        for (int i = 0; i < 1; i++) {
            candidateBins.add(new Level3_Bin(190, 190, 100));
            candidateBins.add(new Level3_Bin(210, 210, 210));
            candidateBins.add(new Level3_Bin(370, 340, 140));
            candidateBins.add(new Level3_Bin(370, 370, 250));
            candidateBins.add(new Level3_Bin(390, 185, 100));
            candidateBins.add(new Level3_Bin(390, 380, 100));
            candidateBins.add(new Level3_Bin(390, 380, 200));
            candidateBins.add(new Level3_Bin(390, 380, 380));
            candidateBins.add(new Level3_Bin(440, 440, 190));
            candidateBins.add(new Level3_Bin(570, 130, 160));
            candidateBins.add(new Level3_Bin(570, 260, 160));
            candidateBins.add(new Level3_Bin(570, 260, 240));
            candidateBins.add(new Level3_Bin(570, 260, 320));
            candidateBins.add(new Level3_Bin(570, 400, 260));
            candidateBins.add(new Level3_Bin(600, 350, 190));

        }
    }

    //set bin as final bin
    public static Level3_Bin setFinalBin(ArrayList<Level3_Bin> candidateBins, Level3_Bin finalBin, int BinChkr, double totalVolChkr, int i) {
        finalBin = candidateBins.get(i);
        BinChkr = finalBin.getNumOfBin();
        totalVolChkr = finalBin.getTotalEmptyVol();
//        System.out.println("finalBin changed " + finalBin.getBinIdentifier());
        return finalBin;
    }

    //find maximum number of boxes that can be placed in the bin.
    public static void chkMaxBox(Level2_Box box, Level3_Bin bin) throws IloException {
        //System.out.println("Upper bound: " + calcUpperBound(box, bin));
        Solver solver = new Solver(box, calcUpperBound(box, bin), bin);
        int numBoxPerLvl = solver.optimize(false);
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
            bin.setTotalEmptyVol(bin.getVolume() - (numOrderedBox * box.getVolume()));
        } else {
            if (bin.getMaxBoxNum() == 0) {
                bin.setNumOfBin(Integer.MAX_VALUE);
                bin.setRemBox(numOrderedBox);
                bin.setTotalEmptyVol(bin.getEmptyVol());
            } else {
                bin.setNumOfBin(numOrderedBox / bin.getMaxBoxNum());
                bin.setRemBox(numOrderedBox % bin.getMaxBoxNum());
                bin.setTotalEmptyVol(bin.getEmptyVol() * bin.getNumOfBin());
            }
        }
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
