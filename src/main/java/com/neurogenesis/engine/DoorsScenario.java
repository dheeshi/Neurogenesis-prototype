package com.neurogenesis.engine;

import com.neurogenesis.model.Neuron;

import java.util.*;

public class DoorsScenario {
    private final Map<String, Integer> costs = new HashMap<>();
    private final Random rnd = new Random();

    public DoorsScenario() {
        shuffleCosts();
    }

    public void shuffleCosts() {
        // Assign random cost (lower is better)
        costs.put("A", rnd.nextInt(10) + 1);
        costs.put("B", rnd.nextInt(10) + 1);
        costs.put("C", rnd.nextInt(10) + 1);
    }

    public Result evaluate(Neuron chosen) {
        if (chosen == null) return new Result(false, "?");

        int chosenCost = costs.getOrDefault(chosen.getLabel(), 10);
        String bestDoor = costs.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("?");

        boolean success = chosen.getLabel().equals(bestDoor);
        return new Result(success, bestDoor);
    }

    public static class Result {
        private final boolean success;
        private final String best;
        public Result(boolean success, String best) {
            this.success = success; this.best = best;
        }
        public boolean success() { return success; }
        public String bestLabel() { return best; }
    }
}
