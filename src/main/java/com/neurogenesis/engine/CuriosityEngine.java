package com.neurogenesis.engine;

import com.neurogenesis.model.Neuron;

import java.util.List;
import java.util.Random;

public class CuriosityEngine {
    public enum DecisionType { LOGICAL, CURIOUS }

    public static class Choice {
        public final Neuron neuron;
        public final DecisionType type;
        public Choice(Neuron n, DecisionType t) {
            this.neuron = n;
            this.type = t;
        }
    }

    private final Random rnd = new Random();

    public Choice choose(List<Neuron> outputs, double curiosityRate) {
        if (outputs == null || outputs.isEmpty()) return new Choice(null, DecisionType.LOGICAL);

        // Logical = choose neuron with strongest weight
        Neuron best = outputs.stream()
                .max((a, b) -> Double.compare(a.getActivation(), b.getActivation()))
                .orElse(outputs.get(0));

        if (rnd.nextDouble() < curiosityRate) {
            // Curiosity: pick a random one instead of best
            return new Choice(outputs.get(rnd.nextInt(outputs.size())), DecisionType.CURIOUS);
        }
        return new Choice(best, DecisionType.LOGICAL);
    }
}
