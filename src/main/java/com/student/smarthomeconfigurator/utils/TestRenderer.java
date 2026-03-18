package com.student.smarthomeconfigurator.utils;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Room;
import com.student.smarthomeconfigurator.devices.*;
import com.student.smarthomeconfigurator.modules.renderer.*;
import com.student.smarthomeconfigurator.modules.simulator.*;
import com.student.smarthomeconfigurator.database.DatabaseManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.stage.Screen;

import java.util.Optional;

public class TestRenderer extends Application {
    private Project project;
    private MapRenderer currentRenderer;
    private DeviceSimulator currentSimulator;
    private DatabaseManager dbManager;

    private BorderPane root;
    private Pane renderPane;
    private Label statusLabel;
    private AnimationTimer uiUpdater;
    private SplitPane splitPane;

    @Override
    public void start(Stage primaryStage) {
        createTestProject();
        dbManager = DatabaseManager.getInstance();

        root = new BorderPane();

        splitPane = new SplitPane();

        VBox leftPanel = createLeftPanel();
        leftPanel.setMinWidth(250);
        leftPanel.setMaxWidth(500);

        renderPane = new Pane();
        renderPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");
        renderPane.setMinWidth(400);

        splitPane.getItems().addAll(leftPanel, renderPane);
        splitPane.setDividerPositions(0.2);

        root.setCenter(splitPane);

        HBox bottomPanel = new HBox(20);
        bottomPanel.setStyle("-fx-padding: 10; -fx-background-color: #e0e0e0;");
        bottomPanel.setMinHeight(40);

        statusLabel = new Label("Готов к работе");
        statusLabel.setStyle("-fx-font-size: 13px;");

        Label sizeLabel = new Label();
        sizeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bottomPanel.getChildren().addAll(statusLabel, spacer, sizeLabel);
        root.setBottom(bottomPanel);

        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        Scene scene = new Scene(root, screenWidth * 0.8, screenHeight * 0.8);

        primaryStage.setTitle("Smart Home Configurator");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();

        sizeLabel.setText(String.format("Размер: %.0f x %.0f",
                primaryStage.getWidth(), primaryStage.getHeight()));

        switchRenderer(new Canvas2DRenderer());
        startUiUpdater();

        primaryStage.setOnCloseRequest(e -> {
            stopSimulation();
            if (currentRenderer != null) {
                currentRenderer.clear();
            }
            if (dbManager != null) {
                dbManager.close();
            }
            Platform.exit();
            System.exit(0);
        });

        renderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (currentRenderer != null) {
                Platform.runLater(() -> {
                    currentRenderer.render(project, renderPane);
                });
            }
        });

        renderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (currentRenderer != null) {
                Platform.runLater(() -> {
                    currentRenderer.render(project, renderPane);
                });
            }
        });

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            sizeLabel.setText(String.format("Размер: %.0f x %.0f",
                    primaryStage.getWidth(), primaryStage.getHeight()));
        });
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(12);
        leftPanel.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");
        leftPanel.setPrefWidth(280);

        Label titleLabel = new Label("SMART HOME CONFIGURATOR");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        leftPanel.getChildren().add(titleLabel);

        leftPanel.getChildren().add(new Separator());

        Label renderLabel = new Label("РЕНДЕРЕР:");
        renderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        leftPanel.getChildren().add(renderLabel);

        Button btn2D = new Button("2D Canvas");
        btn2D.setMaxWidth(Double.MAX_VALUE);
        btn2D.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btn2D.setOnAction(e -> switchRenderer(new Canvas2DRenderer()));

        Button btnLWJGL = new Button("3D");
        btnLWJGL.setMaxWidth(Double.MAX_VALUE);
        btnLWJGL.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnLWJGL.setOnAction(e -> {
            LWJGLRenderer renderer = new LWJGLRenderer();
            if (currentSimulator != null) {
                renderer.setSimulator(currentSimulator);
            }
            switchRenderer(renderer);
        });

        Button btnSVG = new Button("Экспорт SVG");
        btnSVG.setMaxWidth(Double.MAX_VALUE);
        btnSVG.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnSVG.setOnAction(e -> {
            SvgExporter exporter = new SvgExporter();
            exporter.render(project, renderPane);
            exporter.exportToFile("plan.svg");
            showAlert("Экспорт", "Файл plan.svg сохранён!");
        });

        leftPanel.getChildren().addAll(btn2D, btnLWJGL, btnSVG);
        leftPanel.getChildren().add(new Separator());

        Label simLabel = new Label("СИМУЛЯТОР:");
        simLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        leftPanel.getChildren().add(simLabel);

        Button btnRandom = new Button("Случайная");
        btnRandom.setMaxWidth(Double.MAX_VALUE);
        btnRandom.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnRandom.setOnAction(e -> switchSimulator(new RandomSimulator()));

        Button btnSmart = new Button("Умный дом");
        btnSmart.setMaxWidth(Double.MAX_VALUE);
        btnSmart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnSmart.setOnAction(e -> switchSimulator(new SmartHomeSimulator()));

        Button btnManual = new Button("Ручное");
        btnManual.setMaxWidth(Double.MAX_VALUE);
        btnManual.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnManual.setOnAction(e -> switchSimulator(new ManualSimulator()));

        Button btnStop = new Button("Стоп");
        btnStop.setMaxWidth(Double.MAX_VALUE);
        btnStop.setStyle("-fx-background-color: #ffcccc; -fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnStop.setOnAction(e -> stopSimulation());

        leftPanel.getChildren().addAll(btnRandom, btnSmart, btnManual, btnStop);
        leftPanel.getChildren().add(new Separator());

        Label dbLabel = new Label("БАЗА ДАННЫХ:");
        dbLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        leftPanel.getChildren().add(dbLabel);

        Button btnSave = new Button("Сохранить проект");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (project != null && dbManager != null) {
                dbManager.saveProject(project);
                showAlert("Сохранение", "Проект сохранён!");
            }
        });

        Button btnLoad = new Button("Загрузить проект");
        btnLoad.setMaxWidth(Double.MAX_VALUE);
        btnLoad.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnLoad.setOnAction(e -> loadProjectDialog());

        leftPanel.getChildren().addAll(btnSave, btnLoad);
        leftPanel.getChildren().add(new Separator());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        leftPanel.getChildren().add(spacer);

        return leftPanel;
    }

    private void createTestProject() {
        project = new Project("Мой Умный Дом");

        Room livingRoom = new Room("Гостиная", 50, 50, 300, 200);
        livingRoom.addDevice(new LightSensor(livingRoom.getId()));
        livingRoom.addDevice(new TemperatureSensor(livingRoom.getId()));
        livingRoom.addDevice(new Lamp(livingRoom.getId()));
        livingRoom.addDevice(new Lamp(livingRoom.getId(), true));
        project.addRoom(livingRoom);

        Room bedroom = new Room("Спальня", 400, 50, 250, 180);
        bedroom.addDevice(new LightSensor(bedroom.getId()));
        bedroom.addDevice(new Lamp(bedroom.getId()));
        bedroom.addDevice(new MotionSensor(bedroom.getId()));
        project.addRoom(bedroom);

        Room kitchen = new Room("Кухня", 50, 300, 280, 220);
        kitchen.addDevice(new TemperatureSensor(kitchen.getId()));
        kitchen.addDevice(new Lamp(kitchen.getId()));
        kitchen.addDevice(new SmokeSensor(kitchen.getId()));
        project.addRoom(kitchen);

        Room bathroom = new Room("Ванная", 400, 300, 200, 150);
        bathroom.addDevice(new HumiditySensor(bathroom.getId()));
        bathroom.addDevice(new Lamp(bathroom.getId()));
        project.addRoom(bathroom);
    }

    private void switchRenderer(MapRenderer renderer) {
        if (currentRenderer != null) {
            currentRenderer.clear();
        }

        this.currentRenderer = renderer;
        renderPane.getChildren().clear();

        Platform.runLater(() -> {
            currentRenderer.render(project, renderPane);
            statusLabel.setText("Рендерер: " + renderer.getName());
        });
    }

    private void switchSimulator(DeviceSimulator simulator) {
        stopSimulation();
        this.currentSimulator = simulator;
        simulator.startSimulation(project);

        if (currentRenderer instanceof LWJGLRenderer) {
            ((LWJGLRenderer) currentRenderer).setSimulator(simulator);
        }

        statusLabel.setText("Симулятор: " + simulator.getName());
    }

    private void stopSimulation() {
        if (currentSimulator != null) {
            currentSimulator.stopSimulation();
            currentSimulator = null;
        }
        statusLabel.setText("Симуляция остановлена");
    }

    private void startUiUpdater() {
        uiUpdater = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) { lastUpdate = now; return; }
                if ((now - lastUpdate) > 500_000_000) {
                    if (currentRenderer != null &&
                            (currentRenderer instanceof Canvas2DRenderer)) {
                        currentRenderer.render(project, renderPane);
                    }
                    lastUpdate = now;
                }
            }
        };
        uiUpdater.start();
    }

    private void loadProjectDialog() {
        if (dbManager == null) return;
        java.util.List<String> projectIds = dbManager.getAllProjectIds();
        if (projectIds.isEmpty()) {
            showAlert("Загрузка", "Нет сохранённых проектов!");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(projectIds.get(0), projectIds);
        dialog.setTitle("Загрузка проекта");
        dialog.setHeaderText("Выберите проект:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            String projectId = selected.split(" \\(")[0];
            Project loaded = dbManager.loadProject(projectId);
            if (loaded != null) {
                project = loaded;
                if (currentRenderer != null) {
                    currentRenderer.render(project, renderPane);
                }
                showAlert("Загрузка", "Проект загружен!");
            }
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(root.getScene().getWindow());
            alert.show();
        });
    }

    @Override
    public void stop() {
        if (uiUpdater != null) uiUpdater.stop();
        stopSimulation();
        if (currentRenderer != null) {
            currentRenderer.clear();
        }
        if (dbManager != null) dbManager.close();
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        launch(args);
    }
}