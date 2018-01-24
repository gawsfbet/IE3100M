/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Logic.Solver;
import Model.BinStats;
import Model.Level2_Box;
import Model.Level3_Bin;
import Model.Order;
import Model.PackingConfig;
import Utils.FileUtils;
import ilog.concert.IloException;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Kevin-Notebook
 */
public class Main {

    public static final double MAX_WEIGHT = 30;
    public static final int numOrderedBox = 1000;
    public static ArrayList<BinStats> binStats = new ArrayList<>();
    public static ArrayList<PackingConfig> configs = new ArrayList<>();

    public static void main(String[] args) {
        Level2_Box box = new Level2_Box(190, 186, 23, 0.65);
        Order order = new Order(box, numOrderedBox); //in mm and g
        //Level2_Box box = new Level2_Box(190, 186, 23, 0.65); //in mm and g  
        //Level3_Bin bin = new Level3_Bin(570, 400, 260);
        //int binHeight = bin.getHeight();
        ArrayList<Level3_Bin> binList = new ArrayList<>();
        addBins(binList);
        
        binList.forEach((bin) -> {
            try {
                binStats.add(getStats(box, bin));
            } catch (IloException ex) {
                ex.printStackTrace();
            }
        });
        
        binStats.forEach((binStat) -> {
            determineConfig(order, binStat);
        });
        
        Collections.sort(configs);
        
        for (PackingConfig config : configs) {
            System.out.println(config);
            System.out.println(config.getTotalEmptyVol());
            System.out.println();
        }

        
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
    private static BinStats getStats(Level2_Box box, Level3_Bin bin) throws IloException {
        Solver solver = new Solver(box, calcUpperBound(box, bin), bin);
        int quantityPerLayer = solver.optimize(false);
        
        int totalQuantity = quantityPerLayer * (bin.getHeight() / box.getHeight());
        
        if (totalQuantity * box.getWeight() > MAX_WEIGHT) {
            totalQuantity = (int) (MAX_WEIGHT / box.getWeight());
        }
        
        int emptyVolume = (bin.getVolume()) - (totalQuantity * box.getVolume());
        
        /*System.out.println("Number of boxes for bin type " + bin.toString() + ": " + totalQuantity);
        System.out.println("Empty space for bin type " + bin.toString() + ": " + emptyVolume);*/
        
        return new BinStats(bin, quantityPerLayer, totalQuantity, emptyVolume);
    }

    //find how many bins needed for the given order
    private static void determineConfig(Order order, BinStats binStats) {
        Level2_Box box = order.getBox();
        
        PackingConfig config;
        
        if (order.getQuantity() < binStats.getTotalQuantity()) { //one box can fit everything
            int emptyVolPerBin = binStats.getBin().getVolume() - (order.getQuantity() * box.getVolume());
            config = new PackingConfig(binStats.getBin(), 
                    order.getQuantity(), 
                    1, 
                    emptyVolPerBin, 
                    0);
            
            configs.add(config);
        } else if (binStats.getTotalQuantity() == 0) { //level 2 item cannot fit in the bin
            //do nothing
        } else {
            int remainingBoxes = order.getQuantity() % binStats.getTotalQuantity();
            
            config = new PackingConfig(binStats.getBin(), 
                    binStats.getTotalQuantity(), 
                    order.getQuantity() / binStats.getTotalQuantity(), 
                    binStats.getEmptyVolume(), 
                    remainingBoxes);
            
            if (config.getRemainderBoxes() != 0) {
                config.setLastBin(determineRemainderBin(box, config));
                config.setTotalEmptyVol(config.getTotalEmptyVol() + (config.getLastBin().getVolume() - box.getVolume() * config.getRemainderBoxes()));
                
            }
            
            configs.add(config);
        }
    }
    
    private static Level3_Bin determineRemainderBin(Level2_Box box, PackingConfig config) {
        Level3_Bin lastBin = null;
        int emptyVol, minEmptyVol = Integer.MAX_VALUE;
        
        for (BinStats binStat : binStats) {
            if (binStat.getTotalQuantity() >= config.getRemainderBoxes()) { //if the bin can contain all the remainder boxes
                emptyVol = binStat.getBin().getVolume() - box.getVolume() * config.getRemainderBoxes();
                if (emptyVol < minEmptyVol) {
                    lastBin = binStat.getBin();
                    minEmptyVol = emptyVol;
                }
            }
        }
        
        return lastBin;
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
