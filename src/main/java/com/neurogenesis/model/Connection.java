package com.neurogenesis.model;

public class Connection {
    private final Neuron from;
    private final Neuron to;
    private double weight;

    // NEW: for pruning/visuals
    private int usage = 0;
    private int lastUsedTick = -9999;

    public Connection(Neuron from, Neuron to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Neuron getFrom() { return from; }
    public Neuron getTo() { return to; }
    public double getWeight() { return weight; }
    public void setWeight(double w) { this.weight = w; }

    public int getUsage() { return usage; }
    public void bumpUsage() { this.usage++; }
    public int getLastUsedTick() { return lastUsedTick; }
    public void setLastUsedTick(int t) { this.lastUsedTick = t; }
}
