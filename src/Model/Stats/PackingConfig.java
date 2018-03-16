/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

import Logic.BinStatsCalculator;
import Model.Order;
import Model.Product.Level3_Bin;


public class PackingConfig /*implements Comparable<PackingConfig>*/ {
    private final Order order;
    private final BinStats mainBinStats;
    

    private BinStats lastBinStats;

    private long totalEmptyVol;
    private int totalBins;
    
    private double relativeVol;
    private double relativeBinCount;
    
    public PackingConfig (Order order, BinStats mainBinStats) {
        this.order = order;
        this.mainBinStats = mainBinStats;
    }
    
    public void setRelativeVol(double relativeVol){
        this.relativeVol = relativeVol;
    }
    
    public void setRelativeBinCount(double relativeBinCount){
        this.relativeBinCount = relativeBinCount;
    }
    
    public double getRelativeVol(){
        return this.relativeVol;
    }
    
    public double getRelativeBinCount(){
        return this.relativeBinCount;
    }
    
    public Order getOrder() {
        return this.order;
    }
    
    public BinStats getMainBinStats() {
        return this.mainBinStats;
    }

    public int getTotalBins() {
        return this.totalBins;
    }
    
    public BinStats getLastBinStats() {
        return this.lastBinStats;
    }

    public int getTotalBinsInclRemainder() {
        return this.getRemainderBoxes() == 0 ? this.totalBins : this.totalBins + 1;
    }
    
    public int getNumberOfMainBins() {
        return this.order.getQuantity() / this.mainBinStats.getTotalQuantity();
    }

    public int getRemainderBoxes() {
        return this.order.getQuantity() % this.mainBinStats.getTotalQuantity();
    }

    public Level3_Bin getLastBin() {
        return this.lastBinStats.getBin();
    }

    public long getTotalEmptyVol() {
        return this.totalEmptyVol;
    }
    
    public void setAttributes(BinStats lastBinStats, int totalBins) {
        setLastBinStats(lastBinStats);
        setTotalBins(totalBins);
        
        if (lastBinStats == null) {//no remainder bins, either only 1 bin is needed or the boxes can fit into the main bins with no leftover
            if (totalBins == 1) { //case where only 1 bin is needed
                setTotalEmptyVol((long) this.mainBinStats.getBin().getTrimmedVolume(BinStatsCalculator.getBuffer(), BinStatsCalculator.getBufferBothSides()) - (this.order.getQuantity() * this.order.getBox().getVolume()));
            } else { //case where there are no leftovers
                setTotalEmptyVol((long) totalBins * this.mainBinStats.getEmptyVolume());
            }
        } else {
            setTotalEmptyVol(((long) totalBins * this.mainBinStats.getEmptyVolume()) + ((long) lastBinStats.getBin().getTrimmedVolume(BinStatsCalculator.getBuffer(), BinStatsCalculator.getBufferBothSides()) - (this.getRemainderBoxes() * this.order.getBox().getVolume())));
        }
    }
    
    private void setLastBinStats(BinStats lastBinStats) {
        this.lastBinStats = lastBinStats;
    }
    
    private void setTotalBins(int totalBins) {
        this.totalBins = totalBins;
    }
    
    private void setTotalEmptyVol(long totalEmptyVol) {
        this.totalEmptyVol = totalEmptyVol;
    }
    
    public String display() {
        String mainBinInfo = "Main Shipping Carton box:\n" +
                "Name:\t\t" + this.mainBinStats.getBin().getFullName() + "\n" +
                "Dimensions:\t\t" + this.mainBinStats.getBin().getDimensions() + "\n" +
                "Quantity:\t\t" + this.getNumberOfMainBins() + "\n" +
                "Layers perbox:\t" + this.mainBinStats.getBin().getHeight() / this.mainBinStats.getBox().getHeight();
        
        String remBinInfo;
        if (lastBinStats != null) {
            remBinInfo = "Remaining product boxes to be packed: " + this.getRemainderBoxes() + "\n" + 
                    "Shipping Carton box for remaining products:\n" +
                    "Name:\t\t" + this.lastBinStats.getBin().getFullName();
        } else {
            remBinInfo = "No remaining shipping box needed";
        }
        
        return mainBinInfo + "\n\n" + remBinInfo;
    }

    @Override
    public String toString() {

        String info = "Bin type: " + this.mainBinStats.getBin().toString() 
                + " Boxes per Bin: " + this.mainBinStats.getTotalQuantity()
                + " Bins Needed: " + totalBins + " empty vol: " + this.mainBinStats.getEmptyVolume();
        
        return info + (lastBinStats == null ? " Last Bin not needed" : "\n" + lastBinStats.getBin().toString() + " to contain remaining " + this.getRemainderBoxes() + " boxes");
    }

}
