package com.neurogenesis.engine;

import com.neurogenesis.model.NeuralNetwork;
import com.neurogenesis.model.Neuron;
import com.neurogenesis.model.NeuralNetwork.DecisionType;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Now supports two modes:
 *  - neuronOnlyMode = true  -> spiking + Hebbian + pruning (biological-like)
 *  - neuronOnlyMode = false -> your earlier "3 doors curiosity demo"
 */
public class SimulationEngine {
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final NeuralNetwork net;

    private ScheduledFuture<?> task;
    private volatile boolean neuronOnlyMode = true; // default to biological-like

    // --- doors demo pieces (kept)
    private final CuriosityEngine chooser = new CuriosityEngine();
    private final DoorsScenario scenario = new DoorsScenario();
    private final FeedbackLoop feedback  = new FeedbackLoop();
    private volatile double curiosityRate = 0.25;

    public SimulationEngine(NeuralNetwork net) { this.net = net; }

    public void setModeNeuronOnly(boolean on) { this.neuronOnlyMode = on; }
    public boolean isNeuronOnlyMode() { return neuronOnlyMode; }
    public void setCuriosityRate(double v) { this.curiosityRate = Math.max(0, Math.min(1, v)); }
    public void shuffleScenario() { scenario.shuffleCosts(); }

    public void start(long periodMs, Consumer<String> logSink) {
        stop();
        task = exec.scheduleAtFixedRate(() -> {
            String log = stepOnce();
            if (logSink != null) logSink.accept(log);
        }, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    public String stepOnce() {
        return neuronOnlyMode ? stepNeuronOnly() : stepDoorsDemo();
    }

    private String stepNeuronOnly() {
        return net.tickNeuronOnly();
    }

    private String stepDoorsDemo() {
        StringBuilder sb = new StringBuilder();
        sb.append(net.tickNeuronOnly()); // keep neurons alive underneath

        List<Neuron> outputs = net.getOutputNeurons();
        CuriosityEngine.Choice choice = chooser.choose(outputs, curiosityRate);
        Neuron chosen = choice.neuron;

        DoorsScenario.Result res = scenario.evaluate(chosen);
        feedback.apply(net, chosen, res.success());

        DecisionType uiType = (choice.type == CuriosityEngine.DecisionType.CURIOUS)
                ? DecisionType.CURIOUS : DecisionType.LOGICAL;
        net.setLastDecision(chosen != null ? chosen.getId() : null, uiType, res.success());

        if (chosen != null) {
            String tag = (choice.type == CuriosityEngine.DecisionType.CURIOUS) ? "[C]" : "[L]";
            sb.append(tag).append(" Chose ").append(chosen.getLabel())
              .append(String.format(" | best=%s", res.bestLabel()))
              .append(String.format(" | success=%s\n", res.success()));
        } else {
            sb.append("[!] No OUTPUT neurons\n");
        }
        return sb.toString();
    }

    public void step(Consumer<String> logSink) {
        String log = stepOnce();
        if (logSink != null) logSink.accept(log);
    }

    public void stop() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    public void shutdown() {
        stop();
        exec.shutdown();
    }
}
