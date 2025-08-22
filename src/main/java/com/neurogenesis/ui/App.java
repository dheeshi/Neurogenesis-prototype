package com.neurogenesis.ui;

import com.neurogenesis.engine.SimulationEngine;
import com.neurogenesis.model.NeuralNetwork;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {
    private final NeuralNetwork net = new NeuralNetwork();
    private final SimulationEngine engine = new SimulationEngine(net);
    private TextArea logArea;
    private NetworkView view;

    @Override
    public void start(Stage stage) {
        net.seed();

        // Left controls
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(12));
        controls.setPrefWidth(280);

        Button start = new Button("Start");
        Button step  = new Button("Step Once");
        Button stop  = new Button("Stop");
        Button shuffle = new Button("Shuffle Door Costs");

        Slider speed = new Slider(50, 1000, 250);
        speed.setShowTickMarks(true); speed.setShowTickLabels(true);
        speed.setBlockIncrement(50);
        Label speedLbl = new Label("Tick (ms):");

        // Mode toggle
        CheckBox neuronOnly = new CheckBox("Neuron-Only Mode (biological-like)");
        neuronOnly.setSelected(true);
        neuronOnly.selectedProperty().addListener((obs, oldV, on) -> engine.setModeNeuronOnly(on));

        // Optional: curiosity rate (only relevant in doors demo)
        Label curLbl = new Label("Curiosity (doors demo):");
        Slider curiosity = new Slider(0.0, 1.0, 0.25);
        curiosity.setShowTickMarks(true); curiosity.setShowTickLabels(true);
        curiosity.valueProperty().addListener((o,ov,nv) -> engine.setCuriosityRate(nv.doubleValue()));

        start.setOnAction(e -> engine.start((long)speed.getValue(), this::appendLog));
        step.setOnAction(e -> engine.step(this::appendLog));
        stop.setOnAction(e -> engine.stop());
        shuffle.setOnAction(e -> {
            engine.shuffleScenario();
            appendLog("[~] Shuffled door costs\n");
        });

        controls.getChildren().addAll(
                start, step, stop,
                speedLbl, speed,
                neuronOnly,
                curLbl, curiosity,
                shuffle
        );

        // Center: graph view
        view = new NetworkView(net);
        Pane center = new StackPane(view);
        center.setPrefSize(720, 480);
        center.setStyle("-fx-background-color: #f5f7fb;");

        // Bottom logs
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);

        BorderPane root = new BorderPane();
        root.setLeft(controls);
        root.setCenter(center);
        root.setBottom(logArea);

        Scene scene = new Scene(root, 1040, 700);
        stage.setTitle("Biological-Style Neural Prototype (spiking + Hebbian + pruning)");
        stage.setScene(scene);
        stage.show();

        // Repaint at ~60fps
        new AnimationTimer() {
            @Override public void handle(long now) { view.redraw(); }
        }.start();

        stage.setOnCloseRequest(e -> {
            engine.shutdown();
            Platform.exit();
        });
    }

    private void appendLog(String msg) {
        Platform.runLater(() -> {
            logArea.appendText(msg);
            if (logArea.getText().length() > 12000) {
                logArea.deleteText(0, 4000);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

