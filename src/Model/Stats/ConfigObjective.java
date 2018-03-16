/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

/**
 *
 * @author Kevin
 */
public class ConfigObjective implements Comparable<ConfigObjective> {
    private final PackingConfig config;
    private final double objective;

    public ConfigObjective(PackingConfig config, double objective) {
        this.config = config;
        this.objective = objective;
    }

    public PackingConfig getConfig() {
        return this.config;
    }

    public double getObjective() {
        return this.objective;
    }

    @Override
    public int compareTo(ConfigObjective other) {
        return this.objective >= other.objective ? -1 : 1;
    }
}