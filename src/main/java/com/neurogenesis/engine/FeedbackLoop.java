package com.neurogenesis.engine;

import com.neurogenesis.model.NeuralNetwork;
import com.neurogenesis.model.Neuron;

public class FeedbackLoop {
    private static final double LEARN_RATE = 0.1;

    public void apply(NeuralNetwork net, Neuron chosen, boolean success) {
        if (chosen == null) return;

        if (success) {
            chosen.adjustWeight(LEARN_RATE);
        } else {
            chosen.adjustWeight(-LEARN_RATE);
        }
    }
}
