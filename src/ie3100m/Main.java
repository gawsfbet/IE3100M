/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Gui.TestGui;
import Logic.BinStatsCalculator;
import Logic.PackingConfigCalculator;
import Logic.QuantitySolver;
import Model.Stats.BinStats;
import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Order;
import Model.Stats.ConfigObjective;
import Model.Stats.PackingConfig;
import Model.Stats.RankSystem;
import Utils.FileUtils;
import ilog.concert.IloException;
import java.io.File;
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

    private final int qty;
    private final int length;
    private final int width;
    private final int height;
    private final double weight;
    private final int buffer; //in mm
    
    private boolean bufferBothSides;
    
    private final double numCoeff = 0.5;
    private final double volCoeff = 0.5;
    
    public Main(int qty, int length, int width, int height, double weight, int buffer, boolean bufferBothSides) {
        this.qty = qty;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.buffer = buffer;
        this.bufferBothSides = bufferBothSides;
    }

    public PackingConfig run() {
        /**
         * TODO: put order stats as input
         */
        Level2_Box box = new Level2_Box(length, width, height, weight);
        Order order = new Order(box, qty); //in mm and g
        BinStatsCalculator.setBuffer(buffer);
        BinStatsCalculator.setBufferBothSides(bufferBothSides);

        ArrayList<Level3_Bin> binList = new ArrayList<>();

        ArrayList<BinStats> allBinStats = new ArrayList<>();
        ArrayList<PackingConfig> configs = new ArrayList<>();
        
        try {
            binList = FileUtils.loadBinTypes("boxes.csv");

            for (Level3_Bin bin : binList) {
                allBinStats.add(new BinStats(box, bin));
            }
            BinStatsCalculator.setStatsForAllBins(allBinStats);

            for (BinStats binStat : allBinStats) {
                if (binStat.getTotalQuantity() > 0) {
                    configs.add(new PackingConfig(order, binStat));
                }
            }
            PackingConfigCalculator.setAllConfigs(configs, allBinStats);

            PackingConfig bestConfig = determineBestConfig(configs, numCoeff, volCoeff);

            if (bestConfig == null) {
                System.out.println("No suitable config found");
                return null;
            } else {
                BinStatsCalculator.determineArrangement(bestConfig.getMainBinStats());
                if (bestConfig.getLastBinStats() != null) {
                    BinStatsCalculator.determineArrangement(bestConfig.getLastBinStats());
                }
                return bestConfig;
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    /**
     * Determine the best packing configuration based on the sorting order
     *
     * @param packingConfigs the array of possible packing configurations
     * @param numCoeff coefficient for number of bins in the configuration
     * @param volCoeff coefficient for the empty volume in the configuration
     * @return the most desired packing configuration
     */ 
    private static PackingConfig determineBestConfig(ArrayList<PackingConfig> configs, double numCoeff, double volCoeff) {
        ArrayList<ConfigObjective> configObjectives = new ArrayList<>();
        int minNum = Integer.MAX_VALUE;
        long minVol = Long.MAX_VALUE;
        
        for (PackingConfig config : configs) {
            minNum = Math.min(minNum, config.getTotalBinsInclRemainder());
            minVol = Math.min(minVol, config.getTotalEmptyVol());
        }
        
        for (PackingConfig config : configs) {
            configObjectives.add(new ConfigObjective(config, (numCoeff * minNum / config.getTotalBinsInclRemainder()) + (volCoeff * minVol / config.getTotalEmptyVol())));
        }
        
        Collections.sort(configObjectives);
        
        for (ConfigObjective configObjective : configObjectives) {
            if (configObjective.getConfig().getOrder().getQuantity() == 1) {
                return configObjective.getConfig();
            } else if (configObjective.getConfig().getMainBinStats().getTotalQuantity() != 1) {
                return configObjective.getConfig();
            }            
        }
        
        return null;
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
