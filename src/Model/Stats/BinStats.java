/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

import Model.Product.Level3_Bin;

/**
 * The maximum amount of boxes that can be packed into the bin, and the free space remaining.
 */
public class BinStats {
    private final Level3_Bin bin;
    private final int quantityPerLayer;
    private final int totalQuantity;
    private final int emptyVolume;
    
    public BinStats(Level3_Bin bin, int quantityPerLayer, int totalQuantity, int emptyVolume) {
        this.bin = bin;
        this.quantityPerLayer = quantityPerLayer;
        this.totalQuantity = totalQuantity;
        this.emptyVolume = emptyVolume;
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
}
