package com.neurogenesis.ui;

import com.neurogenesis.model.Connection;
import com.neurogenesis.model.NeuralNetwork;
import com.neurogenesis.model.Neuron;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.*;

public class NetworkView extends Group {
    private final NeuralNetwork net;
    private final Map<String, double[]> positions = new HashMap<>();
    private final Random rnd = new Random();

    public NetworkView(NeuralNetwork net) {
        this.net = net;
        setAutoSizeChildren(true);
    }

    public void redraw() {
        getChildren().clear();

        // layout positions (stable per id)
        for (Neuron n : net.getNeurons()) {
            positions.computeIfAbsent(n.getId(), k -> new double[]{
                    80 + rnd.nextDouble()*560, 60 + rnd.nextDouble()*360
            });
        }

        int t = net.getTick();

        // draw edges first (highlight those used this tick)
        for (Connection c : net.getConnections()) {
            double[] p1 = positions.get(c.getFrom().getId());
            double[] p2 = positions.get(c.getTo().getId());
            if (p1 == null || p2 == null) continue;

            Line line = new Line(p1[0], p1[1], p2[0], p2[1]);
            boolean usedNow = (c.getLastUsedTick() == t);
            line.setStroke(usedNow ? Color.DODGERBLUE : Color.GRAY);
            line.setStrokeWidth(Math.max(1.0, c.getWeight()*3.2));
            getChildren().add(line);
        }

        // draw nodes, fire = orange, recent fire = gold fade, otherwise green/grey
        for (Neuron n : net.getNeurons()) {
            double[] p = positions.get(n.getId());
            double radius = (n.getRole() == Neuron.Role.OUTPUT) ? 18 : 14;

            Circle circle = new Circle(p[0], p[1], radius);

            Color fill;
            if (n.isFired()) {
                fill = Color.ORANGE;
            } else {
                int ago = t - n.getLastFiredTick();
                if (ago >= 0 && ago < 8) fill = Color.GOLD; // recent fire glow
                else if (n.getActivation() == 0.0) fill = Color.DARKSLATEGRAY;
                else fill = Color.SEAGREEN;
            }
            circle.setFill(fill);

            // outline OUTPUT neurons lightly (doors demo, optional)
            if (n.getRole() == Neuron.Role.OUTPUT) {
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(2.0);
            }

            String lbl = (n.getLabel() != null ? n.getLabel()+" / " : "") + n.getId();
            Tooltip.install(circle, new Tooltip(
                    lbl + String.format("  act=%.2f", n.getActivation())
            ));
            getChildren().add(circle);
        }
    }
}
