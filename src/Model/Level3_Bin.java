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
public class Level3_Bin {
    private int length;
    private int width;
    private int height;
    private int _maxBoxNum;
    private double _totalEmptyVol;
    private double _emptyVol;
    private int _numOfBins;
    private int _remBoxNum;
    private String binIdentifier;
    
    public Level3_Bin(int length, int width, int height) {
        this.length = length;
        this.width = width;
        this.height = height;
        this._maxBoxNum = 0;
        this.binIdentifier = length + "x" + width + "x" + height;
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
    
    public String getBinIdentifier(){
        return this.binIdentifier;
    }
    
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
        return length + " X " + width + " X " + height;
    }
}
