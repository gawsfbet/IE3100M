/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Logic.Solver;
import Model.Stats.BinStats;
import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Order;
import Model.Stats.PackingConfig;
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

    public static void main(String[] args) {
        Level2_Box box = new Level2_Box(190, 186, 23, 0.65);
        Order order = new Order(box, numOrderedBox); //in mm and g
        //Level2_Box box = new Level2_Box(190, 186, 23, 0.65); //in mm and g  
        //Level3_Bin bin = new Level3_Bin(570, 400, 260);
        //int binHeight = bin.getHeight();
        ArrayList<Level3_Bin> binList = new ArrayList<>();
        ArrayList<PackingConfig> configs = new ArrayList<>();
        
        addBins(binList);
        
        binList.stream().forEach((bin) -> {
            try {
                binStats.add(calculateStats(box, bin));
            } catch (IloException ex) {
                ex.printStackTrace();
            }
        });
        
        binStats.stream().forEach((binStat) -> {
            PackingConfig config = determineConfig(order, binStat);
            if (config != null) {
                configs.add(config);
            }
        });
        
        Collections.sort(configs);
        
        for (PackingConfig config : configs) {
            if (config.getTotalBoxesPerBin() != 1) {
                System.out.println("Chosen configuration:");
                System.out.println(config);
                //System.out.println(config.getTotalEmptyVol());
                //System.out.println();
                break;
            }
        }

        
    }

    //add bins to arraylist
    public static void addBins(ArrayList<Level3_Bin> candidateBins) {
        candidateBins.addAll(FileUtils.getBinList());
    }
    
    //find maximum number of boxes that can be placed in the bin, with cplex API.
    private static BinStats calculateStats(Level2_Box box, Level3_Bin bin) throws IloException {
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
    private static PackingConfig determineConfig(Order order, BinStats binStats) {
        Level2_Box box = order.getBox();
        
        PackingConfig config = null;
        
        if (order.getQuantity() < binStats.getTotalQuantity()) { //one box can fit everything
            int emptyVolPerBin = binStats.getBin().getVolume() - (order.getQuantity() * box.getVolume());
            config = new PackingConfig(binStats.getBin(), 
                    order.getQuantity(), 
                    1, 
                    emptyVolPerBin, 
                    0);
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
        }
        
        return config;
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
