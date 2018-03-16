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

            ArrayList<PackingConfig> binsByNumbers = new ArrayList<>(configs);
            ArrayList<PackingConfig> binsByVolume = new ArrayList<>(configs);

            PackingConfig bestConfig = determineBestConfig(binsByNumbers, binsByVolume);

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
     * @return the most desired packing configuration
     */ 
    private static PackingConfig determineBestConfig(ArrayList<PackingConfig> binsByNumbers, ArrayList<PackingConfig> binsByVolume) {
//        System.out.println("testing inside determine");
        Collections.sort(binsByNumbers, (a, b) -> {
            if (a.getTotalBinsInclRemainder() == b.getTotalBinsInclRemainder()) {
                return a.getTotalEmptyVol() <= b.getTotalEmptyVol() ? -1 : 1;
            } else {
                return a.getTotalBinsInclRemainder() - b.getTotalBinsInclRemainder();
            }
        });
        
        

        Collections.sort(binsByVolume, (a, b) -> {
            if (a.getMainBinStats().getEmptyVolume() == b.getMainBinStats().getEmptyVolume()) {
                return a.getTotalBinsInclRemainder() <= b.getTotalBinsInclRemainder() ? -1 : 1;
            } else {
                return a.getMainBinStats().getEmptyVolume() - b.getMainBinStats().getEmptyVolume();
            }
        });
//        System.out.println("sorted 2 arrays by num and vol");
        double rankPoints;
        ArrayList<RankSystem> rankBins = new ArrayList<>();
        
        /*
        Normalize score of both bincount and volume from 0 to 1
        */
        int lowestCount = binsByNumbers.get(0).getTotalBinsInclRemainder();
        int lowestVolume = binsByVolume.get(0).getMainBinStats().getEmptyVolume();
        for(PackingConfig config: binsByNumbers){
            int binCount = config.getTotalBinsInclRemainder();
            double relativeBinCount = (double) lowestCount / binCount;
            config.setRelativeBinCount(relativeBinCount);
        }
        
        for (PackingConfig config: binsByVolume){
            int volCount = config.getMainBinStats().getEmptyVolume();
            double relativeVol = (double) lowestVolume / volCount;
            config.setRelativeVol(relativeVol);
        }

        for (int i = 0; i < binsByNumbers.size(); i++) {
            for (int j = 0; j < binsByVolume.size(); j++) {
                if (binsByNumbers.get(i).getMainBinStats().getBin().getName() == binsByVolume.get(j).getMainBinStats().getBin().getName()) {
                    rankPoints = binsByNumbers.get(i).getRelativeBinCount() + binsByVolume.get(j).getRelativeVol();
                    RankSystem rank = new RankSystem(binsByVolume.get(j), binsByNumbers.get(i).getRelativeBinCount(), binsByVolume.get(j).getRelativeVol(), rankPoints);
                    rankBins.add(rank);
                }
            }
        }
        
        Collections.sort(rankBins);
        Collections.reverse(rankBins);
        
        
        for (RankSystem rank : rankBins) {
            System.out.println(rank.toString());
//            System.out.println(rank.getConfig().getMainBinStats().getTotalQuantity());
        }
        for (RankSystem rank : rankBins) {
            if (rank.getConfig().getOrder().getQuantity() == 1) {
                return rank.getConfig();
            } else if (rank.getConfig().getMainBinStats().getTotalQuantity() != 1) {
                return rank.getConfig();
            }            
        }
        return null;
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
