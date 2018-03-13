/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Stats.BinStats;
import Model.Stats.BoxArrangement;
import Model.Stats.CplexSolution;
import ilog.concert.IloException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class containing methods to determine the statistics for filling a Level 3 Bin full of Level 2 Boxes
 * @author Kevin-Notebook
 */
public class BinStatsCalculator {
    private static double MAX_WEIGHT = 30;
    private static int buffer = 0;
    private static boolean bufferBothSides;
    
    public static void setWeight(double maxWeight) {
        BinStatsCalculator.MAX_WEIGHT = maxWeight;
    }
    
    public static void setBuffer(int buffer) {
        BinStatsCalculator.buffer = buffer;
    }
    
    public static int getBuffer() {
        return BinStatsCalculator.buffer;
    }
    
    public static void setBufferBothSides(boolean bufferBothSides) {
        BinStatsCalculator.bufferBothSides = bufferBothSides;
    }
    
    public static boolean getBufferBothSides() {
        return BinStatsCalculator.bufferBothSides;
    }
    
    /**
     * Sets the statistics for all the elements of a BinStats array.
     * @param allBinStats The array of BinStats whose attributes are to be calculated
     */
    public static void setStatsForAllBins(ArrayList<BinStats> allBinStats) {
        allBinStats.stream().forEach((binStats) -> {
            try {
                setStatsForBin(binStats);
            } catch (IloException ex) {
                Logger.getLogger(BinStatsCalculator.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        return;
    }
    
    /**
     * Determines the maximum number of the specified level 2 boxes that can fit into the given level 3 bin.
     * @param binStats the BinStats whose attributes are to be determined
     * @throws IloException 
     */
    public static void setStatsForBin(BinStats binStats) throws IloException {
        Level2_Box box = binStats.getBox();
        Level3_Bin bin = binStats.getBin();
        
        QuantitySolver solver = new QuantitySolver(box, calcUpperBound(box, bin, buffer, bufferBothSides), bin, buffer, bufferBothSides);
        
        int quantityPerLayer = solver.optimize(false);
        int totalQuantity = bufferBothSides ? quantityPerLayer * ((bin.getHeight() - 2 * buffer) / box.getHeight()) : quantityPerLayer * ((bin.getHeight() - buffer) / box.getHeight());
        if (totalQuantity * box.getWeight() > MAX_WEIGHT) {
            totalQuantity = (int) (MAX_WEIGHT / box.getWeight());
        }
        
        binStats.setAttributes(quantityPerLayer, totalQuantity, buffer, bufferBothSides);
        return;
    }
    
    public static void determineArrangement(BinStats binStats) throws IloException {
        Level2_Box box = binStats.getBox();
        Level3_Bin bin = binStats.getBin();
        
        CoordsSolver solver = new CoordsSolver(box, binStats.getQuantityPerLayer(), bin, buffer, bufferBothSides);
        
        BoxArrangement[] arrangement = solver.align(false);
        
        binStats.setArrangementForOneLayer(arrangement);
    }
    
    private static int calcUpperBound(Level2_Box box, Level3_Bin bin, int buffer, boolean bufferBothSides) {
        return bin.getTrimmedBaseArea(buffer, bufferBothSides) / box.getBaseArea();
    }
}
