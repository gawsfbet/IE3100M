/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import Model.Order;
import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Stats.BinStats;
import Model.Stats.PackingConfig;
import java.util.ArrayList;

/**
 *
 * @author Kevin-Notebook
 */
public class PackingConfigCalculator {
    /**
     * Given COMPLETE list of bins, calculate config for each bin if it were the main bin
     * @param order
     * @param allBinStats
     * @return 
     */
    public static void setAllConfigs(ArrayList<PackingConfig> packingConfigs, ArrayList<BinStats> candidateRemainingBins) {
        packingConfigs.stream().forEach((packingConfig) -> {
            if (packingConfig.getMainBinStats().getTotalQuantity() > 0) {
                setConfig(packingConfig, candidateRemainingBins);
            }
        });
    }
    
    /**
     * Calculates the number of bins to use for the given order, as well as determine the remaining bin to use (if needed)
     * @param order The order of level 2 boxes and the quantity to be packed
     * @param binStats The statistics of the bin should it be packed to the brim
     * @param candidateRemainingBins The list of candidate bins to use for the remaining bin
     * @return The packing configuration for the given Level3_Bin, if it were to be used as the main bin
     */
    public static void setConfig(PackingConfig packingConfig, ArrayList<BinStats> candidateRemainingBins) {
        assert packingConfig.getMainBinStats().getTotalQuantity() > 0;
        
        Order order = packingConfig.getOrder();
        BinStats binStats = packingConfig.getMainBinStats();
        
        Level2_Box box = order.getBox();
        
        if (order.getQuantity() < binStats.getTotalQuantity()) { //one box can fit everything
            /*int emptyVolPerBin = binStats.getBin().getVolume() - (order.getQuantity() * box.getVolume());
            config = new PackingConfig(binStats.getBin(), 
                    order.getQuantity(), 
                    1, 
                    emptyVolPerBin, 
                    0);*/
            packingConfig.setAttributes(null, 1);
        } else if (binStats.getTotalQuantity() == 0) { //level 2 item cannot fit in the bin
            //do nothing
        } else {
            Level3_Bin lastBin = null;
            if (packingConfig.getRemainderBoxes() != 0) {
                lastBin = determineRemainderBin(packingConfig, candidateRemainingBins);
            }
            
            packingConfig.setAttributes(lastBin, order.getQuantity() / binStats.getTotalQuantity());
        }
    }
    
    /**
     * Determines the hest bin to use for the remaining boxes
     * @param box The level 2 boxes to be packed
     * @param int the amount of remaining boxes
     * @param candidateRemainingBins The list of candidate bins to use for the remaining bin
     * @return The bin to be used to pack the remaining boxes of the order
     */
    private static Level3_Bin determineRemainderBin(PackingConfig packingConfig, ArrayList<BinStats> candidateRemainingBins) {
        Level2_Box box = packingConfig.getOrder().getBox();
        Level3_Bin lastBin = null;
        int emptyVol, minEmptyVol = Integer.MAX_VALUE, remainder = packingConfig.getRemainderBoxes();
        
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
}
