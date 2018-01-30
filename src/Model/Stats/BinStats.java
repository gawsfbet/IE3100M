/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;

/**
 * The maximum amount of boxes that can be packed into the bin, and the free space remaining.
 */
public class BinStats {
    private final Level2_Box box;
    private final Level3_Bin bin;
    
    private int quantityPerLayer;
    private int totalQuantity;
    private int emptyVolume;
    
    public BinStats(Level2_Box box, Level3_Bin bin) {
        this.box = box;
        this.bin = bin;
    }
    
    public Level2_Box getBox() {
        return this.box;
    }

    public Level3_Bin getBin() {
        return this.bin;
    }

    public int getQuantityPerLayer() {
        return this.quantityPerLayer;
    }

    public int getTotalQuantity() {
        return this.totalQuantity;
    }

    public int getEmptyVolume() {
        return this.emptyVolume;
    }
    
    public void setAttributes(int quantityPerLayer, int totalQuantity) {
        this.setQuantityPerLayer(quantityPerLayer);
        this.setTotalQuantity(totalQuantity);
        this.setEmptyVolume(bin.getVolume() - (totalQuantity * box.getVolume()));
    }
    
    private void setQuantityPerLayer(int quantityPerLayer) {
        this.quantityPerLayer = quantityPerLayer;
    }
    
    private void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    private void setEmptyVolume(int emptyVolume) {
        this.emptyVolume = emptyVolume;
    }
}
