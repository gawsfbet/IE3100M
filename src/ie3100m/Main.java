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
import java.io.IOException;
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
        
        ArrayList<Level3_Bin> binList = new ArrayList<>();
        ArrayList<BinStats> allBinStats = new ArrayList<>();;
        ArrayList<PackingConfig> configs = new ArrayList<>();;
        
        try {
            binList = FileUtils.loadBinTypes("boxes.csv");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
    
    /**
     * Determines the maximum number of the specified level 2 boxes that can fit into the given level 3 bin.
     * @param box The level 2 boxes to be packed
     * @param binList The array of bins to be used
     * @return the array of BinStats objects for the maximum of level 2 boxes that can fit in each of the bins in the array
     */
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
     * Determines the maximum number of the specified level 2 boxes that can fit into the given level 3 bin.
     * @param box The level 2 boxes to be packed
     * @param bin The level 3 bin to be used
     * @return the BinStats object for the maximum number of level 2 boxes that can fit
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

    /**
     * Given COMPLETE list of bins, calculate config for each bin if it were the main bin
     * @param order
     * @param allBinStats
     * @return 
     */
    private static ArrayList<PackingConfig> determineAllConfigs(Order order, ArrayList<BinStats> allBinStats) {
        ArrayList<PackingConfig> packingConfigs = new ArrayList<>();
        
        allBinStats.stream().forEach((binStat) -> {
            PackingConfig config = determineConfig(order, binStat, allBinStats);
            if (config != null) {
                packingConfigs.add(config);
            }
        });
        
        return packingConfigs;
    }
    
    /**
     * Calculates the number of bins to use for the given order, as well as determine the remaining bin to use (if needed)
     * @param order The order of level 2 boxes and the quantity to be packed
     * @param binStats The statistics of the bin should it be packed to the brim
     * @param candidateRemainingBins The list of candidate bins to use for the remaining bin
     * @return The packing configuration for the given Level3_Bin, if it were to be used as the main bin
     */
    private static PackingConfig determineConfig(Order order, BinStats binStats, ArrayList<BinStats> candidateRemainingBins) {
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
                config.setLastBin(determineRemainderBin(box, config.getRemainderBoxes(), candidateRemainingBins));
                config.setTotalEmptyVol(config.getTotalEmptyVol() + (config.getLastBin().getVolume() - box.getVolume() * config.getRemainderBoxes()));
            }
        }
        
        return config;
    }
    
    /**
     * Determines the hest bin to use for the remaining boxes
     * @param box The level 2 boxes to be packed
     * @param int the amount of remaining boxes
     * @param candidateRemainingBins The list of candidate bins to use for the remaining bin
     * @return The bin to be used to pack the remaining boxes of the order
     */
    private static Level3_Bin determineRemainderBin(Level2_Box box, int remainder, ArrayList<BinStats> candidateRemainingBins) {
        Level3_Bin lastBin = null;
        int emptyVol, minEmptyVol = Integer.MAX_VALUE;
        
        for (BinStats binStat : candidateRemainingBins) {
            if (binStat.getTotalQuantity() >= remainder) { //if the bin can contain all the remainder boxes
                emptyVol = binStat.getBin().getVolume() - box.getVolume() * remainder;
                if (emptyVol < minEmptyVol) {
                    lastBin = binStat.getBin();
                    minEmptyVol = emptyVol;
                }
            }
        }
        
        return lastBin;
    }
    
    /**
     * Determine the best packing configuration based on the sorting order
     * @param packingConfigs the array of possible packing configurations
     * @return the most desired packing configuration
     */
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
