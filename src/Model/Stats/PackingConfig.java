/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

import Model.Product.Level3_Bin;

/**
 * Represents a packing configuration.
 * Contains the total number of (main) bins to use, as well as the bin to use for the remaining boxes, as well as total free volume
 */
public class PackingConfig implements Comparable<PackingConfig> {
    private final Level3_Bin mainBin;
    private Level3_Bin lastBin;
    
    private long totalEmptyVol;
    private final int emptyVolPerBin; //might not need
    private final int totalBoxesPerBin;
    private int totalBins;
    private final int remainderBoxes;
    
    public PackingConfig (Level3_Bin mainBin, int totalBoxesPerBin, int totalBins, int emptyVolPerBin, int remainderBoxes) {
        this.mainBin = mainBin;
        
        this.totalBoxesPerBin = totalBoxesPerBin;
        this.totalBins = totalBins;
        this.remainderBoxes = remainderBoxes;
        this.emptyVolPerBin = emptyVolPerBin;
        this.totalEmptyVol = (long) totalBins * emptyVolPerBin;
    }
    
    public int getTotalBins() {
        return this.totalBins;
    }
    
    public int getTotalBinsInclRemainder() {
        return remainderBoxes == 0 ? this.totalBins : this.totalBins + 1;
    }
    
    public int getTotalBoxesPerBin() {
        return this.totalBoxesPerBin;
    }
    
    public int getRemainderBoxes() {
        return this.remainderBoxes;
    }
    
    public Level3_Bin getLastBin() {
        return this.lastBin;
    }
    
    public long getTotalEmptyVol() {
        return this.totalEmptyVol;
    }
    
    public void setLastBin(Level3_Bin lastBin) {
        this.lastBin = lastBin;
    }
    
    public void setTotalBins(int totalBins) {
        this.totalBins = totalBins;
    }
    
    public void setTotalEmptyVol(long totalEmptyVol) {
        this.totalEmptyVol = totalEmptyVol;
    }
    
    /**
     * Modify this method if another attribute is to be minimised
     * @param other
     * @return 
     */
    @Override
    public int compareTo(PackingConfig other) {
        if (this.getTotalBinsInclRemainder() == other.getTotalBinsInclRemainder()) {
            return this.getTotalEmptyVol() <= other.getTotalEmptyVol() ? -1 : 1;
        } else {
            return this.getTotalBinsInclRemainder() - other.getTotalBinsInclRemainder();
        }
    }
    
    @Override
    public String toString() {
        String info = "Bin type: " + this.mainBin.toString() 
                + "\nBoxes per Bin: " + totalBoxesPerBin
                + "\nBins Needed: " + totalBins;
        
        return info + (lastBin == null ? "\nLast Bin not needed" : "\n" + lastBin.toString() + " to contain remaining " + remainderBoxes + " boxes");
    }
    
}
