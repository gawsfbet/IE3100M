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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin-Notebook
 */
public class Main {

    public static final double MAX_WEIGHT = 30;

    public static void main(String[] args) {
        /**
         * TODO: put order stats as input
         */
        int numOrderedBox = 1000;
        Level2_Box box = new Level2_Box(190, 186, 23, 0.65);
        Order order = new Order(box, numOrderedBox); //in mm and g
        //Level2_Box box = new Level2_Box(190, 186, 23, 0.65); //in mm and g  
        //Level3_Bin bin = new Level3_Bin(570, 400, 260);
        //int binHeight = bin.getHeight();
        ArrayList<Level3_Bin> binList;
        ArrayList<BinStats> allBinStats;
        ArrayList<PackingConfig> configs;
        
        binList = FileUtils.getBinTypesFromFile();
        allBinStats = calculateStatsForAllBins(box, binList);
        configs = determineAllConfigs(order, allBinStats);
        
        PackingConfig bestConfig = determineBestConfig(configs);
        
        if (bestConfig == null) {
            System.out.println("No suitable config found");
        } else {
            System.out.println("Chosen config:");
            System.out.println(bestConfig);
        }
        
    }
    
    private static ArrayList<BinStats> calculateStatsForAllBins(Level2_Box box, ArrayList<Level3_Bin> binList) {
        ArrayList<BinStats> binStats = new ArrayList<>();
        
        binList.stream().forEach((bin) -> {
            try {
                binStats.add(calculateStatsForBin(box, bin));
            } catch (IloException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        return binStats;
    }
    
    /**
     * 
     * @param box The level 2 boxes to be packed
     * @param bin The level 3 bin to be used
     * @return the number of boxes that can be packed into the bin, as well as the free space
     * @throws IloException 
     */
    private static BinStats calculateStatsForBin(Level2_Box box, Level3_Bin bin) throws IloException {
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

    //Given COMPLETE list of bins, calculate config for each bin if it were the main bin
    private static ArrayList<PackingConfig> determineAllConfigs(Order order, ArrayList<BinStats> binStats) {
        ArrayList<PackingConfig> packingConfigs = new ArrayList<>();
        
        binStats.stream().forEach((binStat) -> {
            PackingConfig config = determineConfig(order, binStat, binStats);
            if (config != null) {
                packingConfigs.add(config);
            }
        });
        
        return packingConfigs;
    }
    
    //find how many bins needed for the given order
    private static PackingConfig determineConfig(Order order, BinStats binStats, ArrayList<BinStats> allBinStats) {
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
                config.setLastBin(determineRemainderBin(box, config, allBinStats));
                config.setTotalEmptyVol(config.getTotalEmptyVol() + (config.getLastBin().getVolume() - box.getVolume() * config.getRemainderBoxes()));
                
            }
        }
        
        return config;
    }
    
    private static Level3_Bin determineRemainderBin(Level2_Box box, PackingConfig config, ArrayList<BinStats> allBinStats) {
        Level3_Bin lastBin = null;
        int emptyVol, minEmptyVol = Integer.MAX_VALUE;
        
        for (BinStats binStat : allBinStats) {
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
    
    private static PackingConfig determineBestConfig(ArrayList<PackingConfig> packingConfigs) {
        Collections.sort(packingConfigs);
        
        for (PackingConfig config : packingConfigs) {
            if (config.getTotalBoxesPerBin() != 1) {
                return config;
            }
        }
        
        return null;
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
