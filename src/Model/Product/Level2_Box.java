/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Product;

/**
 *
 * @author Kevin-Notebook
 */
public class Level2_Box {
    private final int length;
    private final int width;
    private final int height;
    private final double weight;
    private final String name;
    
    public Level2_Box(int length, int width, int height, double weight) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.name = "";
    }
    
    public Level2_Box(int length, int width, int height, double weight, String name) {
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.name = name;
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
    
    public String getName(){
        return this.name.equals("") ? "no name" : this.name;
    }
    
    public String getDimensions() {
        return String.format("%d X %d X %d mm", this.length, this.width, this.height);
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", this.getName(), this.getDimensions());
    }
}
