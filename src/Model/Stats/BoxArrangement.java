/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

/**
 *
 * @author Kevin-Notebook
 */
public class BoxArrangement {
    private final int x;
    private final int y;
    private final boolean isHorizontal;
    
    public BoxArrangement(int x, int y, boolean isHorizontal) {
        this.x = x;
        this.y = y;
        this.isHorizontal = isHorizontal;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public boolean isHorizontal() {
        return this.isHorizontal;
    }
    
    @Override
    public String toString() {
        return String.format("(%d, %d, %b)", this.x, this.y, this.isHorizontal);
    }
}
