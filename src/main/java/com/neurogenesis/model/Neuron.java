package com.neurogenesis.model;

import java.util.concurrent.atomic.AtomicLong;

public class Neuron {
    public enum Role { REGULAR, OUTPUT }

    private static final AtomicLong SEQ = new AtomicLong(0);

    private final String id;
    private double activation;           // 0..1 visualization value
    private boolean probabilistic;       // legacy UI hint (kept)
    private boolean alive = true;

    // roles/labels (kept for optional "doors demo")
    private Role role = Role.REGULAR;
    private String label;                // e.g., "Door A"

    // --- NEW: synaptic/selection weight for reinforcement
    private double weight = 0.5;         // 0..1, default neutral
    // (weight is distinct from connection weights; used by feedback if desired)

    // --- NEW/EXISTING: spiking dynamics
    private boolean fired;               // fired at current tick?
    private boolean nextFired;           // staging for 2-phase update
    private int lastFiredTick = -9999;   // for UI glow/fade

    // stochastic excitability
    private double bias = (Math.random()-0.5) * 0.3; // slight individuality
    private double threshold = 0.6;                   // firing threshold

    // curiosity/novelty memory (kept; useful for later)
    private int visits = 0;

    public Neuron() {
        this.id = "N" + SEQ.incrementAndGet();
        this.activation = Math.random();
    }

    // --- getters/setters
    public String getId() { return id; }
    public double getActivation() { return activation; }
    public void setActivation(double a) { this.activation = a; }
    public boolean isProbabilistic() { return probabilistic; }
    public void setProbabilistic(boolean b) { this.probabilistic = b; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean a) { this.alive = a; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    // --- weight accessors (NEW)
    public double getWeight() { return weight; }
    public void setWeight(double w) { this.weight = Math.max(0.0, Math.min(1.0, w)); }

    /**
     * Adjust the neuron's internal weight by delta (positive = strengthen,
     * negative = weaken). Keeps weight within [0,1].
     */
    public void adjustWeight(double delta) {
        this.weight += delta;
        if (this.weight < 0.0) this.weight = 0.0;
        if (this.weight > 1.0) this.weight = 1.0;
    }

    // --- spiking helpers
    public boolean isFired() { return fired; }
    public void setFired(boolean fired) { this.fired = fired; }
    public boolean isNextFired() { return nextFired; }
    public void setNextFired(boolean nextFired) { this.nextFired = nextFired; }
    public int getLastFiredTick() { return lastFiredTick; }
    public void setLastFiredTick(int t) { this.lastFiredTick = t; }

    public double getBias() { return bias; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double t) { this.threshold = t; }

    public int getVisits() { return visits; }
    public void bumpVisit() { this.visits++; }
    public double getNovelty() { return 1.0 / (1.0 + visits); }
}
