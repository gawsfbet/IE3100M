/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author Kevin-Notebook
 */
public class PackingConfig implements Comparable<PackingConfig> {
    private Level3_Bin mainBin;
    private Level3_Bin lastBin;
    private int totalEmptyVol;
    private int emptyVol;
    private int totalBoxes;
    private int totalBins;
    private int remainderBoxes;
    
    public PackingConfig(Level3_Bin mainBin) {
        this.mainBin = mainBin;
        
        this.emptyVol = mainBin.getVolume();
        this.totalBoxes = 0;
        this.remainderBoxes = 0;
    }
    
    public PackingConfig(Level3_Bin mainBin, int totalBins, int emptyVol) {
        this.mainBin = mainBin;
        
        this.emptyVol = emptyVol;
        this.totalBoxes = totalBins;
        this.remainderBoxes = 0;
    }
    
    public void setMainBin(Level3_Bin mainBin) {
        this.mainBin = mainBin;
    }
    
    public void setLastBin(Level3_Bin lastBin) {
        this.lastBin = lastBin;
    }
    
    public void setTotalBoxes(int totalBoxes) {
        this.totalBoxes = totalBoxes;
    }
    
    public void setTotalBins(int totalBins) {
        this.totalBins = totalBins;
    }
    
    public void setRemainderBoxes(int remainderBoxes) {
        this.remainderBoxes = remainderBoxes;
    }
    
    public void setEmptyVol(int emptyVol) {
        this.emptyVol = emptyVol;
    }
    
    public void setTotalEmptyVol(int totalEmptyVol) {
        this.totalEmptyVol = totalEmptyVol;
    }
    
    @Override
    public int compareTo(PackingConfig other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
