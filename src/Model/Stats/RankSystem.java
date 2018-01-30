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
    private int numRank;
    private int volRank;
    private int rankPoints;
    
    public RankSystem (PackingConfig config, int numRank, int volRank, int rankPoints){
        this.config = config;
        this.rankPoints = rankPoints;
        this.numRank = numRank;
        this.volRank = volRank;
    }
    
    public int getNumRank(){
        return this.numRank;
    }
    
    public int getVolRank(){
        return this.volRank;
    }
    
    public PackingConfig getConfig() {
        return this.config;
    }
    
    public int getRankPoints(){
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
            return this.getConfig().getTotalBinsInclRemainder() - other.getConfig().getTotalBinsInclRemainder();
        } else {
            return this.getRankPoints() - other.getRankPoints();
        }
    }
    
    @Override
    public String toString(){
        String info  = "rank: " + this.rankPoints + " numRank: " + this.numRank + " volRank: " + this.volRank + " ";
        return info + this.config.toString();
    }
    
}

