package com.neurogenesis.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NeuralNetwork {
    private final List<Neuron> neurons = new CopyOnWriteArrayList<>();
    private final List<Connection> connections = new CopyOnWriteArrayList<>();
    private final Random rnd = new Random();

    // --- simulation tick
    private int tick = 0;
    public int getTick() { return tick; }

    // --- optional demo decision memory (for UI ring when "doors mode" on)
    public enum DecisionType { LOGICAL, CURIOUS }
    private String lastChosenId = null;
    private DecisionType lastDecisionType = null;
    private boolean lastSuccess = false;
    public void setLastDecision(String neuronId, DecisionType type, boolean success) {
        this.lastChosenId = neuronId;
        this.lastDecisionType = type;
        this.lastSuccess = success;
    }
    public String getLastChosenId() { return lastChosenId; }
    public DecisionType getLastDecisionType() { return lastDecisionType; }
    public boolean wasLastSuccess() { return lastSuccess; }

    // --- parameters (tunable from engine/UI later if you want sliders)
    private final double noiseStd = 0.35;     // randomness in firing
    private final double spontaneous = 0.03;  // base chance to fire anyway
    private final double eta = 0.10;          // Hebbian strengthen
    private final double decay = 0.01;        // weight decay per tick
    private final double wMin = 0.03, wMax = 1.8;
    private final int pruneGrace = 80;        // if not used for N ticks, allow prune
    private final double pruneBelow = 0.08;   // and weight below this

    public void seed() {
        if (!neurons.isEmpty()) return;

        // create a small core
        Neuron n1 = addNeuron();
        Neuron n2 = addNeuron();
        Neuron n3 = addNeuron();
        Neuron n4 = addNeuron();

        connect(n1, n2, 0.4);
        connect(n2, n3, 0.5);
        connect(n3, n4, 0.6);
        connect(n1, n3, 0.2);
        connect(n2, n4, 0.3);

        // keep "doors" neurons around for the optional demo (OUTPUT role)
        Neuron doorA = addNeuron(); doorA.setRole(Neuron.Role.OUTPUT); doorA.setLabel("Door A");
        Neuron doorB = addNeuron(); doorB.setRole(Neuron.Role.OUTPUT); doorB.setLabel("Door B");
        Neuron doorC = addNeuron(); doorC.setRole(Neuron.Role.OUTPUT); doorC.setLabel("Door C");
        connect(n2, doorA, rnd.nextDouble());
        connect(n3, doorB, rnd.nextDouble());
        connect(n4, doorC, rnd.nextDouble());
    }

    public Neuron addNeuron() {
        Neuron n = new Neuron();
        neurons.add(n);
        return n;
    }

    public void removeNeuron(Neuron n) {
        n.setAlive(false);
        neurons.remove(n);
        connections.removeIf(c -> c.getFrom() == n || c.getTo() == n);
    }

    public void connect(Neuron a, Neuron b, double w) {
        if (a == b) return;
        connections.add(new Connection(a, b, w));
    }

    public List<Neuron> getNeurons() { return neurons; }
    public List<Connection> getConnections() { return connections; }
    public List<Neuron> getOutputNeurons() {
        List<Neuron> out = new ArrayList<>();
        for (Neuron n : neurons) if (n.getRole() == Neuron.Role.OUTPUT) out.add(n);
        return out;
    }

    // ------------------------------
    // Neuron-only dynamics step
    // ------------------------------
    public synchronized String tickNeuronOnly() {
        tick++;
        StringBuilder log = new StringBuilder();

        // (A) compute inputs from currently fired neurons
        Map<Neuron, Double> inputs = new HashMap<>();
        for (Neuron n : neurons) inputs.put(n, 0.0);

        for (Connection c : connections) {
            if (c.getFrom().isFired()) {
                inputs.put(c.getTo(), inputs.get(c.getTo()) + c.getWeight());
                c.bumpUsage();
                c.setLastUsedTick(tick);
            }
        }

        // (B) decide nextFired probabilistically (sigmoid(input + noise + bias) > threshold)
        int firedCount = 0;
        for (Neuron n : neurons) {
            if (!n.isAlive()) continue;

            double input = inputs.get(n) + n.getBias() + rnd.nextGaussian()*noiseStd;
            double prob = sigmoid(input); // 0..1
            boolean willFire = prob > n.getThreshold() || rnd.nextDouble() < spontaneous;

            n.setNextFired(willFire);
            if (willFire) firedCount++;
        }

        // (C) two-phase commit: set fired, update activations for UI
        for (Neuron n : neurons) {
            n.setFired(n.isNextFired());
            if (n.isFired()) n.setLastFiredTick(tick);
            // activation for color intensity
            n.setActivation(n.isFired() ? 1.0 : Math.max(0.0, n.getActivation()*0.90));
        }

        // (D) Hebbian: if pre fired AND post fired this tick → strengthen; otherwise decay
        int strengthened = 0, decayed = 0, pruned = 0;
        for (Connection c : connections) {
            double w = c.getWeight();

            boolean coActive = c.getFrom().isFired() && c.getTo().isFired();
            if (coActive) {
                w = w + eta * (1.0 - w); // push up toward 1
                strengthened++;
            } else {
                w = w * (1.0 - decay);
                decayed++;
            }
            w = clamp(w, wMin, wMax);
            c.setWeight(w);
        }

        // (E) prune: rarely used & weak edges; avoid pruning edges into OUTPUT to keep demo intact
        Iterator<Connection> it = connections.iterator();
        while (it.hasNext()) {
            Connection c = it.next();
            if (c.getTo().getRole() == Neuron.Role.OUTPUT) continue;
            if (tick - c.getLastUsedTick() > pruneGrace && c.getWeight() < pruneBelow) {
                it.remove();
                pruned++;
            }
        }

        // (F) growth: occasionally add a neuron or a connection
        if (neurons.size() < 24 && rnd.nextDouble() < 0.12) {
            Neuron n = addNeuron();
            // connect from a random currently-fired neuron if available
            Optional<Neuron> maybe = neurons.stream().filter(Neuron::isFired).findAny();
            Neuron src = maybe.orElse(neurons.get(rnd.nextInt(neurons.size())));
            connect(src, n, 0.25 + rnd.nextDouble()*0.3);
            log.append("[+] ").append(n.getId()).append(" added\n");
        }
        if (connections.size() < neurons.size() * 3 && rnd.nextDouble() < 0.20) {
            Neuron a = neurons.get(rnd.nextInt(neurons.size()));
            Neuron b = neurons.get(rnd.nextInt(neurons.size()));
            if (a != b) {
                connect(a, b, 0.15 + rnd.nextDouble()*0.3);
                log.append("[+] synapse ").append(a.getId()).append("→").append(b.getId()).append("\n");
            }
        }

        log.append(String.format("[*] t=%d fired=%d strengthened=%d decayed=%d pruned=%d\n",
                tick, firedCount, strengthened, decayed, pruned));

        return log.toString();
    }

    // legacy random tick (kept in case you still want it)
    public synchronized String tickLegacy() {
        StringBuilder log = new StringBuilder();

        for (Neuron n : neurons) {
            boolean prob = rnd.nextDouble() < 0.15;
            n.setProbabilistic(prob);
            if (!prob) n.setActivation(rnd.nextDouble());
        }
        log.append("[~] Legacy tick (random states)\n");
        return log.toString();
    }

    private static double sigmoid(double x) {
        // numerically safe-ish
        if (x < -12) return 0.0;
        if (x >  12) return 1.0;
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

