/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Logic.Solver;
import Model.Level2_Box;
import Model.Level3_Bin;
import Model.Order;
import Model.PackingConfig;
import Utils.FileUtils;
import ilog.concert.IloException;
import java.util.ArrayList;

/**
 *
 * @author Kevin-Notebook
 */
public class Main {

    public static final int numOrderedBox = 1000;
    public static ArrayList<PackingConfig> configs;

    public static void main(String[] args) {
        Order order = new Order(new Level2_Box(190, 186, 23, 0.65), numOrderedBox); //in mm and g
        //Level2_Box box = new Level2_Box(190, 186, 23, 0.65); //in mm and g  
        //Level3_Bin bin = new Level3_Bin(570, 400, 260);
        //int binHeight = bin.getHeight();
        ArrayList<Level3_Bin> candidateBins = new ArrayList<>();
        addBins(candidateBins);
        
        candidateBins.forEach((bin) -> {
            try {
                chkMaxBox(order, bin);
            } catch (IloException ex) {
                ex.printStackTrace();
            }
        });

        printAllBinStats(candidateBins, order.getBox(), numOrderedBox);

        //Bin Comparison this shit is damn messy.
        Level3_Bin finalBin = candidateBins.get(0);
        int binChkr = finalBin.getNumOfBin();
        double totalVolChkr = finalBin.getTotalEmptyVol();
        
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
                bin.setEmptyVol(bin.getVolume() - (remainderBox * order.getBox().getVolume()));
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

        printFinalBinStats(finalBin, order.getBox(), numOrderedBox);
        printLastBinStats(lastBin, order.getBox(), remainderBox);

    }

    private static void printLastBinStats(Level3_Bin bin, Level2_Box box, int remainderBox) {
        System.out.println("Last Bin:");
        System.out.println("box type: " + box.getBoxIdentifier() + " remainder size: " + remainderBox);
        System.out.print("bin type " + bin.toString());
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
        System.out.print("bin type " + bin.toString());
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
            System.out.print("bin type " + bin.toString());
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
        candidateBins.addAll(FileUtils.getBinList());
    }

    //set bin as final bin
    public static Level3_Bin setFinalBin(ArrayList<Level3_Bin> candidateBins, Level3_Bin finalBin, int BinChkr, double totalVolChkr, int i) {
        finalBin = candidateBins.get(i);
        BinChkr = finalBin.getNumOfBin();
        totalVolChkr = finalBin.getTotalEmptyVol();
//        System.out.println("finalBin changed " + finalBin.getBinIdentifier());
        return finalBin;
    }

    //find maximum number of boxes that can be placed in the bin, with cplex API.
    //after which, find how many bins needed for the given order
    public static void chkMaxBox(Order order, Level3_Bin bin) throws IloException {
        Level2_Box box = order.getBox();
        Solver solver = new Solver(box, calcUpperBound(box, bin), bin);
        int numBoxPerLayer = solver.optimize(false);
        int numLayers = bin.getHeight() / box.getHeight();
        int totalBoxPerBin = numBoxPerLayer * numLayers;
        int emptyVol = (bin.getVolume()) - (totalBoxPerBin * box.getVolume());
        
        PackingConfig config = new PackingConfig(bin, totalBoxPerBin, emptyVol);
        System.out.println("Number of boxes for bin type " + bin.toString() + ": " + totalBoxPerBin);
        System.out.println("Empty space for bin type " + bin.toString() + ": " + emptyVol);
        
        if (order.getQuantity() < totalBoxPerBin) { //one box can fit everything
            config.setEmptyVol(bin.getVolume() - (order.getQuantity() * box.getVolume()));
            config.setTotalBins(1);
            config.setRemainderBoxes(0);
            config.setTotalEmptyVol(bin.getVolume() - (order.getQuantity() * box.getVolume()));
            
            configs.add(config);
        } else if (totalBoxPerBin == 0) { //level 2 item cannot fit in the bin
            config.setTotalBins(Integer.MAX_VALUE);
            config.setRemainderBoxes(numOrderedBox);
            config.setTotalEmptyVol(bin.getEmptyVol());
        } else {
            config.setTotalBins(order.getQuantity() / totalBoxPerBin);
            config.setRemainderBoxes(numOrderedBox % totalBoxPerBin);
            config.setTotalEmptyVol(emptyVol * totalBoxPerBin);
        }
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
