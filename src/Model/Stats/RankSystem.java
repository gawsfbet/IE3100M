/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

import Model.Stats.PackingConfig;

/**
 *
 * @author elbat
 */
public class RankSystem implements Comparable<RankSystem> {
    private PackingConfig config;
    private double numRank;
    private double volRank;
    private double rankPoints;
    
    public RankSystem (PackingConfig config, double numRank, double volRank, double rankPoints){
        this.config = config;
        this.rankPoints = rankPoints;
        this.numRank = numRank;
        this.volRank = volRank;
    }
    
    public double getNumRank(){
        return this.numRank;
    }
    
    public double getVolRank(){
        return this.volRank;
    }
    
    public PackingConfig getConfig() {
        return this.config;
    }
    
    public double getRankPoints(){
        return this.rankPoints;
    }
    
    public void setConfig(PackingConfig config){
        this.config = config;
    }
    
    public void setRankPoints(int rankPoints){
        this.rankPoints = rankPoints;
    }
    
    @Override
    public int compareTo(RankSystem other) {
        if (this.getRankPoints() == other.getRankPoints()) {
            if (this.getConfig().getTotalBinsInclRemainder() < other.getConfig().getTotalBinsInclRemainder()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            if (this.getRankPoints() < other.getRankPoints()){
                return -1;
            } else {
                return 1;
            }
        }
    }
    
    @Override
    public String toString(){
        String info  = "rank: " + this.rankPoints + " numRank: " + this.numRank + " volRank: " + this.volRank + " ";
        return info + this.config.toString();
    }
    
}

