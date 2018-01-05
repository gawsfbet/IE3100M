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
public class Level2_Box {
    private int length;
    private int width;
    private int height;
    
    private double weight;
    
    public Level2_Box(int length, int width, int height, double weight) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
    }
    
    public int getLength() {
        return this.length;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public int getBaseArea() {
        return this.length * this.width;
    }
    
    public int getVolume() {
        return this.length * this.width * this.height;
    }
    
    public double getWeight() {
        return this.weight;
    }
}
