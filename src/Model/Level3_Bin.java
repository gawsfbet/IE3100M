/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.text.DecimalFormat;

/**
 *
 * @author Kevin-Notebook
 */
public class Level3_Bin {
    private int length;
    private int width;
    private int height;
    private int _maxBoxNum;
    private double _totalEmptyVol;
    private double _emptyVol;
    private int _numOfBins;
    private int _remBoxNum;
    private String name;
    
    public Level3_Bin(int length, int width, int height) {
        this.name = "";
        this.length = length;
        this.width = width;
        this.height = height;
        this._maxBoxNum = 0;
    }
    
    public Level3_Bin(String name, int length, int width, int height) {
        this.name = name;
        this.length = length;
        this.width = width;
        this.height = height;
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
    
    public String getName() {
        return this.name;
    }
    
    public String getFullName() {
        return String.format("(DW) CARTON %s", this.name);
    }
    
    public String getDimensions() {
        DecimalFormat decFormat = new DecimalFormat("#.#");
        return String.format("%s X %s X %s cm", 
                decFormat.format((float) this.length / 10), 
                decFormat.format((float) this.width / 10), 
                decFormat.format((float) this.height / 10));
    }
    
    /*public String getBinIdentifier(){
        return this.binIdentifier;
    }*/
    
    public int getMaxBoxNum(){
        return this._maxBoxNum;
    }
    
    public double getEmptyVol(){
        return this._emptyVol;
    }
    
    public int getNumOfBin(){
        return this._numOfBins;
    }
    
    public int getRemBox(){
        return this._remBoxNum;
    }
    
    public double getTotalEmptyVol(){
        return this._totalEmptyVol;
    }
    
    public void setMaxBoxNum(int boxNum){
        _maxBoxNum = boxNum;
    }
    
    public void setEmptyVol(double emptyVol){
        _emptyVol =  emptyVol;
    }
    
    public void setNumOfBin(int binNum){
        _numOfBins = binNum;
    }
    
    public void setRemBox(int remBox){
        _remBoxNum = remBox;
    }
    
    public void setTotalEmptyVol(double totalEmptyVol){
        _totalEmptyVol = totalEmptyVol;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", getFullName(), getDimensions());
    }
}
