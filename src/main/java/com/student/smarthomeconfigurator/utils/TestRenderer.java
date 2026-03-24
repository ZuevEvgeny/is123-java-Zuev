package com.student.smarthomeconfigurator.utils;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Room;
import com.student.smarthomeconfigurator.model.building.*;
import com.student.smarthomeconfigurator.modules.renderer.*;
import com.student.smarthomeconfigurator.modules.simulator.*;
import com.student.smarthomeconfigurator.manager.ProjectManager;
import com.student.smarthomeconfigurator.database.DatabaseManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.stage.Screen;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TestRenderer extends Application {
    private Project project;
    private MapRenderer currentRenderer;
    private DeviceSimulator currentSimulator;
    private DatabaseManager dbManager;
    private ProjectManager projectManager;

    private BuildingProject currentBuildingProject;
    private PlanEditor planEditor;
    private LWJGLRenderer lwjglRenderer;

    private BorderPane root;
    private Pane renderPane;
    private Label statusLabel;
    private AnimationTimer uiUpdater;
    private SplitPane splitPane;

    @Override
    public void start(Stage primaryStage) {
        createTestProject();
        dbManager = DatabaseManager.getInstance();
        projectManager = ProjectManager.getInstance();

        currentBuildingProject = new BuildingProject("Мой дом");
        createTestWalls();

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

        primaryStage.setTitle("Smart Home Configurator - 3D Plan Editor");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();

        sizeLabel.setText(String.format("Размер: %.0f x %.0f",
                primaryStage.getWidth(), primaryStage.getHeight()));

        switchToPlanEditor();
        startUiUpdater();

        primaryStage.setOnCloseRequest(e -> {
            stopSimulation();
            if (lwjglRenderer != null) lwjglRenderer.stopGL();
            if (dbManager != null) dbManager.close();
            Platform.exit();
            System.exit(0);
        });

        renderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (currentRenderer != null && !(currentRenderer instanceof LWJGLRenderer)) {
                Platform.runLater(() -> currentRenderer.render(project, renderPane));
            }
        });

        renderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (currentRenderer != null && !(currentRenderer instanceof LWJGLRenderer)) {
                Platform.runLater(() -> currentRenderer.render(project, renderPane));
            }
        });

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            sizeLabel.setText(String.format("Размер: %.0f x %.0f",
                    primaryStage.getWidth(), primaryStage.getHeight()));
        });
    }

    private void createTestWalls() {
        currentBuildingProject.getWalls().clear();
        currentBuildingProject.getRooms().clear();
        currentBuildingProject.getFurniture().clear();

        WallSegment wall1 = new WallSegment(-5, -5, 5, -5);
        wall1.setHeight(3.0f);
        currentBuildingProject.getWalls().add(wall1);

        WallSegment wall2 = new WallSegment(5, -5, 5, 5);
        wall2.setHeight(3.0f);
        currentBuildingProject.getWalls().add(wall2);

        WallSegment wall3 = new WallSegment(5, 5, -5, 5);
        wall3.setHeight(3.0f);
        currentBuildingProject.getWalls().add(wall3);

        WallSegment wall4 = new WallSegment(-5, 5, -5, -5);
        wall4.setHeight(3.0f);
        currentBuildingProject.getWalls().add(wall4);

        RoomArea room = new RoomArea("Гостиная");
        room.addVertex(-5, -5);
        room.addVertex(5, -5);
        room.addVertex(5, 5);
        room.addVertex(-5, 5);
        currentBuildingProject.getRooms().add(room);
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(12);
        leftPanel.setStyle("-fx-padding: 15; -fx-background-color: #f5f5f5;");
        leftPanel.setPrefWidth(280);

        Label titleLabel = new Label("SMART HOME CONFIGURATOR");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        leftPanel.getChildren().add(titleLabel);
        leftPanel.getChildren().add(new Separator());

        Label renderLabel = new Label("РЕЖИМ:");
        renderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        leftPanel.getChildren().add(renderLabel);

        Button btn2D = new Button("✏️ 2D Редактор плана");
        btn2D.setMaxWidth(Double.MAX_VALUE);
        btn2D.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-color: #2196F3; -fx-text-fill: white;");
        btn2D.setOnAction(e -> switchToPlanEditor());

        Button btn3D = new Button("🎮 3D Просмотр");
        btn3D.setMaxWidth(Double.MAX_VALUE);
        btn3D.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-color: #FF9800; -fx-text-fill: white;");
        btn3D.setOnAction(e -> switchTo3DView());

        leftPanel.getChildren().addAll(btn2D, btn3D);
        leftPanel.getChildren().add(new Separator());

        Label simLabel = new Label("СИМУЛЯТОР:");
        simLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        leftPanel.getChildren().add(simLabel);

        Button btnRandom = new Button("🎲 Случайная");
        btnRandom.setMaxWidth(Double.MAX_VALUE);
        btnRandom.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnRandom.setOnAction(e -> switchSimulator(new RandomSimulator()));

        Button btnSmart = new Button("🏠 Умный дом");
        btnSmart.setMaxWidth(Double.MAX_VALUE);
        btnSmart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnSmart.setOnAction(e -> switchSimulator(new SmartHomeSimulator()));

        Button btnManual = new Button("🖐️ Ручное");
        btnManual.setMaxWidth(Double.MAX_VALUE);
        btnManual.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnManual.setOnAction(e -> switchSimulator(new ManualSimulator()));

        Button btnStop = new Button("⏹️ Стоп");
        btnStop.setMaxWidth(Double.MAX_VALUE);
        btnStop.setStyle("-fx-background-color: #ffcccc; -fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnStop.setOnAction(e -> stopSimulation());

        leftPanel.getChildren().addAll(btnRandom, btnSmart, btnManual, btnStop);
        leftPanel.getChildren().add(new Separator());

        Label projectLabel = new Label("ПРОЕКТ:");
        projectLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        leftPanel.getChildren().add(projectLabel);

        Button btnSave = new Button("💾 Сохранить проект");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnSave.setOnAction(e -> saveCurrentProject());

        Button btnLoad = new Button("📂 Загрузить проект");
        btnLoad.setMaxWidth(Double.MAX_VALUE);
        btnLoad.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand;");
        btnLoad.setOnAction(e -> loadProjectDialog());

        Button btnDeleteProject = new Button("🗑️ УДАЛИТЬ ПРОЕКТ");
        btnDeleteProject.setMaxWidth(Double.MAX_VALUE);
        btnDeleteProject.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDeleteProject.setOnAction(e -> deleteProjectDialog());
        leftPanel.getChildren().add(btnDeleteProject);

        Button btnUpdate3D = new Button("🔄 Обновить 3D");
        btnUpdate3D.setMaxWidth(Double.MAX_VALUE);
        btnUpdate3D.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        btnUpdate3D.setOnAction(e -> update3DFromCurrentPlan());

        leftPanel.getChildren().addAll(btnSave, btnLoad, btnUpdate3D);
        leftPanel.getChildren().add(new Separator());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        leftPanel.getChildren().add(spacer);

        return leftPanel;
    }

    private void update3DFromCurrentPlan() {
        if (planEditor != null) {
            currentBuildingProject = planEditor.getBuildingProject();
            statusLabel.setText("📐 План обновлен: " + currentBuildingProject.getWalls().size() + " стен, " +
                    currentBuildingProject.getRooms().size() + " комнат");

            if (lwjglRenderer != null && currentRenderer instanceof LWJGLRenderer) {
                lwjglRenderer.loadBuildingProject(currentBuildingProject);
            }
        } else {
            statusLabel.setText("❌ Сначала откройте 2D редактор");
        }
    }

    private void deleteProjectDialog() {
        List<ProjectManager.ProjectInfo> projects = projectManager.getProjectList();
        if (projects.isEmpty()) {
            showAlert("Удаление", "Нет проектов для удаления!");
            return;
        }

        ChoiceDialog<ProjectManager.ProjectInfo> dialog = new ChoiceDialog<>(projects.get(0), projects);
        dialog.setTitle("Удаление проекта");
        dialog.setHeaderText("Выберите проект для удаления:");

        Optional<ProjectManager.ProjectInfo> result = dialog.showAndWait();
        result.ifPresent(projectInfo -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение");
            confirm.setHeaderText("Удалить проект?");
            confirm.setContentText("Вы уверены, что хотите удалить проект \"" + projectInfo.name + "\"?");
            confirm.initOwner(root.getScene().getWindow());

            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                try {
                    if ("database".equals(projectInfo.source)) {
                        dbManager.deleteBuildingProject(projectInfo.id);
                    } else {
                        // Удаляем файл
                        java.nio.file.Files.deleteIfExists(
                                java.nio.file.Paths.get("projects/" + projectInfo.id + ".json")
                        );
                    }
                    statusLabel.setText("✅ Проект \"" + projectInfo.name + "\" удален");
                } catch (Exception ex) {
                    statusLabel.setText("❌ Ошибка удаления: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    private void switchToPlanEditor() {
        if (lwjglRenderer != null) {
            lwjglRenderer.stopGL();
            lwjglRenderer = null;
        }

        if (currentRenderer != null) currentRenderer.clear();

        planEditor = new PlanEditor();
        if (currentBuildingProject != null) planEditor.setBuildingProject(currentBuildingProject);
        this.currentRenderer = planEditor;

        Platform.runLater(() -> {
            renderPane.getChildren().clear();
            currentRenderer.render(project, renderPane);
            statusLabel.setText("Режим: 2D Редактор плана");
        });
    }

    private void switchTo3DView() {
        if (planEditor != null) {
            currentBuildingProject = planEditor.getBuildingProject();
            planEditor = null;
        }

        if (lwjglRenderer != null) {
            lwjglRenderer.stopGL();
            lwjglRenderer = null;
        }

        if (currentRenderer != null) currentRenderer.clear();

        lwjglRenderer = new LWJGLRenderer();
        if (currentSimulator != null) lwjglRenderer.setSimulator(currentSimulator);

        if (currentBuildingProject != null) {
            lwjglRenderer.loadBuildingProject(currentBuildingProject);
        }

        this.currentRenderer = lwjglRenderer;

        Platform.runLater(() -> {
            renderPane.getChildren().clear();
            currentRenderer.render(project, renderPane);
            statusLabel.setText("Режим: 3D Просмотр");
        });
    }

    private void saveCurrentProject() {
        if (planEditor != null) {
            currentBuildingProject = planEditor.getBuildingProject();
        }

        if (currentBuildingProject != null) {
            TextInputDialog dialog = new TextInputDialog(currentBuildingProject.getName());
            dialog.setTitle("Сохранение проекта");
            dialog.setHeaderText("Введите название проекта:");
            dialog.setContentText("Название:");
            dialog.initOwner(root.getScene().getWindow());

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    String oldName = currentBuildingProject.getName();
                    currentBuildingProject.setName(name.trim());

                    try {
                        String filename = name.trim().replace(" ", "_") + ".json";
                        projectManager.saveToFile(currentBuildingProject, filename);
                        projectManager.saveToDatabase(currentBuildingProject);
                        statusLabel.setText("✅ Проект сохранен: " + name +
                                " (" + currentBuildingProject.getWalls().size() + " стен)");
                    } catch (Exception e) {
                        statusLabel.setText("❌ Ошибка сохранения: " + e.getMessage());
                        e.printStackTrace();
                        currentBuildingProject.setName(oldName);
                    }
                }
            });
        }
    }

    private void loadProjectDialog() {
        java.util.List<ProjectManager.ProjectInfo> projects = projectManager.getProjectList();
        if (projects.isEmpty()) {
            showAlert("Загрузка", "Нет сохранённых проектов!");
            return;
        }

        ChoiceDialog<ProjectManager.ProjectInfo> dialog = new ChoiceDialog<>(projects.get(0), projects);
        dialog.setTitle("Загрузка проекта");
        dialog.setHeaderText("Выберите проект:");

        Optional<ProjectManager.ProjectInfo> result = dialog.showAndWait();
        result.ifPresent(projectInfo -> {
            BuildingProject loaded = null;
            if ("database".equals(projectInfo.source)) {
                loaded = projectManager.loadFromDatabase(projectInfo.id);
            } else {
                try {
                    loaded = projectManager.loadFromFile(projectInfo.id + ".json");
                } catch (IOException e) {
                    e.printStackTrace();
                    statusLabel.setText("❌ Ошибка загрузки: " + e.getMessage());
                }
            }

            if (loaded != null) {
                currentBuildingProject = loaded;
                statusLabel.setText("✅ Проект загружен: " + loaded.getName() +
                        " (" + loaded.getWalls().size() + " стен)");

                if (currentRenderer instanceof PlanEditor && planEditor != null) {
                    planEditor.setBuildingProject(currentBuildingProject);
                    currentRenderer.render(project, renderPane);
                } else if (currentRenderer instanceof LWJGLRenderer && lwjglRenderer != null) {
                    lwjglRenderer.loadBuildingProject(currentBuildingProject);
                    currentRenderer.render(project, renderPane);
                }
            }
        });
    }

    private void createTestProject() {
        project = new Project("Мой Умный Дом");
        Room livingRoom = new Room("Гостиная", 50, 50, 300, 200);
        project.addRoom(livingRoom);
        Room bedroom = new Room("Спальня", 400, 50, 250, 180);
        project.addRoom(bedroom);
    }

    private void switchSimulator(DeviceSimulator simulator) {
        stopSimulation();
        this.currentSimulator = simulator;
        simulator.startSimulation(project);
        if (lwjglRenderer != null) lwjglRenderer.setSimulator(simulator);
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
        // Таймер только для обновления 2D отображения датчиков (если нужно)
        // PlanEditor перерисовывается по событиям мыши, поэтому не нужно постоянно вызывать render
        uiUpdater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Не перерисовываем PlanEditor автоматически
                if (currentRenderer != null && !(currentRenderer instanceof PlanEditor) && !(currentRenderer instanceof LWJGLRenderer)) {
                    currentRenderer.render(project, renderPane);
                }
            }
        };
        uiUpdater.start();
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
        if (lwjglRenderer != null) lwjglRenderer.stopGL();
        if (dbManager != null) dbManager.close();
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        launch(args);
    }
}