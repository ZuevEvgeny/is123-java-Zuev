package com.student.smarthomeconfigurator.modules.renderer;

import com.student.smarthomeconfigurator.library.ItemCategory;
import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.building.*;
import com.student.smarthomeconfigurator.library.ItemLibrary;
import com.student.smarthomeconfigurator.library.LibraryItem;
import com.student.smarthomeconfigurator.utils.CollisionChecker;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.Cursor;
import java.util.*;

public class PlanEditor implements MapRenderer {

    public enum Tool {
        SELECT, WALL, ROOM, FURNITURE, LAMP, SENSOR, DOOR, WINDOW
    }

    private Project currentProject;
    private BuildingProject buildingProject;
    private Pane container;
    private Canvas canvas;
    private GraphicsContext gc;

    private Tool currentTool = Tool.ROOM;
    private List<Point2DData> currentWallPoints = new ArrayList<>();
    private List<Point2DData> currentRoomPoints = new ArrayList<>();
    private boolean isDrawing = false;

    private LibraryItem selectedFurniture;
    private double mouseWorldX, mouseWorldY;
    private WallSegment selectedWall = null;
    private RoomArea selectedRoom = null;
    private FurnitureItem selectedItem = null;

    private Label statusLabel;
    private ListView<String> furnitureList;
    private VBox toolBox;
    private VBox propertiesPanel;

    // Для перетаскивания стен и углов
    private WallSegment draggingWall = null;
    private Point2DData draggedVertex = null;
    private WallSegment draggedWallForVertex = null;
    private int draggedVertexIndex = -1;
    private double dragStartX, dragStartY;
    private boolean isDragging = false;

    // Для панорамирования и масштабирования
    private double viewOffsetX = 0;
    private double viewOffsetY = 0;
    private double viewScale = 1.0;
    private double lastPanX, lastPanY;
    private boolean isPanning = false;

    // Для превью размещения
    private boolean isPlacementValid = true;
    private float previewX, previewZ;
    private float previewRotation = 0; // градусы

    // Флаг для предотвращения рекурсивных обновлений
    private boolean isUpdatingProperties = false;

    private static final float WORLD_MIN = -12;
    private static final float WORLD_MAX = 12;
    private static final float WORLD_SIZE = WORLD_MAX - WORLD_MIN;
    private static final float SNAP_DISTANCE = 0.5f;

    public PlanEditor() {
        this.buildingProject = new BuildingProject("Новый проект");
    }

    @Override
    public void render(Project project, Pane container) {
        this.currentProject = project;
        this.container = container;

        canvas = new Canvas();
        canvas.widthProperty().bind(container.widthProperty());
        canvas.heightProperty().bind(container.heightProperty());
        gc = canvas.getGraphicsContext2D();

        setupEventHandlers();

        Platform.runLater(() -> {
            container.getChildren().clear();
            container.getChildren().add(canvas);
            createUI();
            draw();
        });
    }

    private void setupEventHandlers() {
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseMoved(this::onMouseMoved);
        canvas.setOnScroll(this::onScroll);

        // Обработка клавиш для поворота
        canvas.setOnKeyPressed(e -> {
            if (currentTool == Tool.FURNITURE || currentTool == Tool.LAMP || currentTool == Tool.SENSOR) {
                if (selectedFurniture != null) {
                    if (e.getCode() == javafx.scene.input.KeyCode.R) {
                        previewRotation += 45;
                        if (previewRotation >= 360) previewRotation -= 360;
                        updatePreviewCollision();
                        draw();
                        if (statusLabel != null) {
                            statusLabel.setText("🔁 Поворот: " + (int)previewRotation + "°");
                        }
                        e.consume();
                    } else if (e.getCode() == javafx.scene.input.KeyCode.SHIFT) {
                        previewRotation -= 45;
                        if (previewRotation < 0) previewRotation += 360;
                        updatePreviewCollision();
                        draw();
                        if (statusLabel != null) {
                            statusLabel.setText("🔁 Поворот: " + (int)previewRotation + "°");
                        }
                        e.consume();
                    }
                }
            }
        });

        canvas.setFocusTraversable(true);
    }

    private void updatePreviewCollision() {
        if (selectedFurniture != null) {
            ItemLibrary lib = ItemLibrary.getInstance();
            LibraryItem libItem = lib.getItem(selectedFurniture.getId());
            if (libItem != null) {
                float width = libItem.getWidth() * selectedFurniture.getDefaultScale();
                float depth = libItem.getDepth() * selectedFurniture.getDefaultScale();
                float rotationRad = (float) Math.toRadians(previewRotation);

                isPlacementValid = !CollisionChecker.checkWallCollision(
                        previewX, previewZ, width, depth, rotationRad,
                        buildingProject.getWalls()
                ) && !CollisionChecker.checkFurnitureCollision(
                        previewX, previewZ, width, depth, rotationRad,
                        buildingProject.getFurniture(), null
                );
            }
        }
    }

    private void onScroll(ScrollEvent e) {
        double delta = e.getDeltaY();
        if (delta == 0) delta = e.getTextDeltaY();
        double zoomFactor = delta > 0 ? 1.1 : 0.9;
        double oldScale = viewScale;
        viewScale *= zoomFactor;
        viewScale = Math.max(0.1, Math.min(5.0, viewScale));

        Point2DData worldBefore = screenToWorld(e.getX(), e.getY());
        viewOffsetX = (viewOffsetX + worldBefore.x) * (viewScale / oldScale) - worldBefore.x;
        viewOffsetY = (viewOffsetY + worldBefore.y) * (viewScale / oldScale) - worldBefore.y;

        draw();
    }

    private Point2DData screenToWorld(double screenX, double screenY) {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        if (canvasWidth == 0 || canvasHeight == 0) return new Point2DData(0, 0);

        double worldX = WORLD_MIN + (screenX / canvasWidth) * WORLD_SIZE;
        double worldY = WORLD_MIN + ((canvasHeight - screenY) / canvasHeight) * WORLD_SIZE;

        worldX = (worldX - viewOffsetX) / viewScale;
        worldY = (worldY - viewOffsetY) / viewScale;

        return new Point2DData(worldX, worldY);
    }

    private Point2DData worldToScreen(double worldX, double worldY) {
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        double scaledX = worldX * viewScale + viewOffsetX;
        double scaledY = worldY * viewScale + viewOffsetY;

        double screenX = ((scaledX - WORLD_MIN) / WORLD_SIZE) * canvasWidth;
        double screenY = canvasHeight - ((scaledY - WORLD_MIN) / WORLD_SIZE) * canvasHeight;
        return new Point2DData(screenX, screenY);
    }

    private Point2DData snapToNearestPoint(double x, double y) {
        for (WallSegment wall : buildingProject.getWalls()) {
            double distToStart = Math.hypot(x - wall.getX1(), y - wall.getZ1());
            if (distToStart < SNAP_DISTANCE) {
                return new Point2DData(wall.getX1(), wall.getZ1());
            }
            double distToEnd = Math.hypot(x - wall.getX2(), y - wall.getZ2());
            if (distToEnd < SNAP_DISTANCE) {
                return new Point2DData(wall.getX2(), wall.getZ2());
            }

            double[] proj = projectPointOnSegment(x, y, wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
            if (proj != null) {
                double distToSegment = Math.hypot(x - proj[0], y - proj[1]);
                if (distToSegment < SNAP_DISTANCE) {
                    return new Point2DData(proj[0], proj[1]);
                }
            }
        }

        for (RoomArea room : buildingProject.getRooms()) {
            for (Point2DData p : room.getVertices()) {
                double dist = Math.hypot(x - p.x, y - p.y);
                if (dist < SNAP_DISTANCE) {
                    return new Point2DData(p.x, p.y);
                }
            }
        }

        return null;
    }

    private double[] projectPointOnSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double ax = px - x1;
        double ay = py - y1;
        double bx = x2 - x1;
        double by = y2 - y1;

        double dot = ax * bx + ay * by;
        double len2 = bx * bx + by * by;

        if (len2 == 0) return null;

        double t = dot / len2;
        if (t < 0 || t > 1) return null;

        double projX = x1 + t * bx;
        double projY = y1 + t * by;
        return new double[]{projX, projY};
    }

    private void createUI() {
        statusLabel = new Label("🏠 Инструмент: Комната (кликайте по углам, клик на первую точку для завершения)");
        statusLabel.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: #ffaa00; -fx-padding: 8; -fx-background-radius: 5; -fx-font-size: 12px;");
        statusLabel.setLayoutX(10);
        statusLabel.setLayoutY(10);
        container.getChildren().add(statusLabel);

        toolBox = new VBox(8);
        toolBox.setStyle("-fx-background-color: rgba(30,30,40,0.9); -fx-padding: 12; -fx-background-radius: 8;");
        toolBox.setLayoutX(10);
        toolBox.setLayoutY(50);
        toolBox.setPrefWidth(160);

        Label toolTitle = new Label("ИНСТРУМЕНТЫ");
        toolTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        toolBox.getChildren().add(toolTitle);

        Button btnSelect = createToolButton("🔍 ВЫБОР", Tool.SELECT, "#607D8B");
        Button btnRoom = createToolButton("🏠 КОМНАТА", Tool.ROOM, "#4CAF50");
        Button btnWall = createToolButton("📏 СТЕНА", Tool.WALL, "#FF5722");
        Button btnFurniture = createToolButton("🪑 МЕБЕЛЬ", Tool.FURNITURE, "#795548");
        Button btnLamp = createToolButton("💡 ЛАМПА", Tool.LAMP, "#FF9800");
        Button btnSensor = createToolButton("📡 ДАТЧИК", Tool.SENSOR, "#00BCD4");
        Button btnDoor = createToolButton("🚪 ДВЕРЬ", Tool.DOOR, "#8B4513");
        Button btnWindow = createToolButton("🪟 ОКНО", Tool.WINDOW, "#87CEEB");

        toolBox.getChildren().addAll(btnSelect, btnRoom, btnWall, btnFurniture, btnLamp, btnSensor, btnDoor, btnWindow);

        Button btnGenerateWalls = new Button("🧱 СТЕНЫ ИЗ КОМНАТЫ");
        btnGenerateWalls.setMaxWidth(Double.MAX_VALUE);
        btnGenerateWalls.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 5;");
        btnGenerateWalls.setOnAction(e -> {
            if (selectedRoom != null) {
                generateWallsFromRoom(selectedRoom);
            } else if (statusLabel != null) {
                statusLabel.setText("⚠️ Сначала выберите комнату инструментом ВЫБОР");
            }
        });
        toolBox.getChildren().add(btnGenerateWalls);

        Button btnDelete = new Button("🗑️ УДАЛИТЬ ВЫБРАННОЕ");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 5;");
        btnDelete.setOnAction(e -> deleteSelected());
        toolBox.getChildren().add(btnDelete);

        Button btnResetView = new Button("🎯 СБРОС ВИДА");
        btnResetView.setMaxWidth(Double.MAX_VALUE);
        btnResetView.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 5;");
        btnResetView.setOnAction(e -> {
            viewOffsetX = 0;
            viewOffsetY = 0;
            viewScale = 1.0;
            draw();
            if (statusLabel != null) statusLabel.setText("👁️ Вид сброшен");
        });
        toolBox.getChildren().add(btnResetView);

        Button btnClear = new Button("🗑️ ОЧИСТИТЬ ВСЁ");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 5;");
        btnClear.setOnAction(e -> clearAll());
        toolBox.getChildren().add(btnClear);

        container.getChildren().add(toolBox);

        VBox furnitureBox = new VBox(5);
        furnitureBox.setStyle("-fx-background-color: rgba(30,30,40,0.9); -fx-padding: 12; -fx-background-radius: 8;");
        furnitureBox.setLayoutX(container.getWidth() - 210);
        furnitureBox.setLayoutY(50);
        furnitureBox.setPrefWidth(200);

        Label furnitureTitle = new Label("📦 БИБЛИОТЕКА");
        furnitureTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        furnitureBox.getChildren().add(furnitureTitle);

        furnitureList = new ListView<>();
        furnitureList.setPrefHeight(300);
        furnitureList.setStyle("-fx-background-color: #3a3a4a; -fx-control-inner-background: #3a3a4a; -fx-text-fill: white;");

        ItemLibrary lib = ItemLibrary.getInstance();
        for (LibraryItem item : lib.getAllItems()) {
            furnitureList.getItems().add(getCategoryIcon(item.getCategory()) + " " + item.getName());
        }

        furnitureList.setOnMouseClicked(e -> {
            int idx = furnitureList.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                selectedFurniture = lib.getAllItems().toArray(new LibraryItem[0])[idx];
                switch (selectedFurniture.getCategory()) {
                    case LAMP: currentTool = Tool.LAMP; break;
                    case SENSOR: currentTool = Tool.SENSOR; break;
                    default: currentTool = Tool.FURNITURE;
                }
                previewRotation = 0;
                canvas.requestFocus();
                if (statusLabel != null) {
                    statusLabel.setText("Размещение: " + selectedFurniture.getName() + " (R/Shift - поворот)");
                }
                updateToolButtonStyles();
            }
        });

        furnitureBox.getChildren().add(furnitureList);
        container.getChildren().add(furnitureBox);

        createPropertiesPanel();

        Label coordLabel = new Label();
        coordLabel.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: #aaa; -fx-padding: 4; -fx-font-size: 10px;");
        coordLabel.setLayoutX(10);
        coordLabel.setLayoutY(container.getHeight() - 30);
        container.getChildren().add(coordLabel);

        container.widthProperty().addListener((obs, old, newVal) -> {
            furnitureBox.setLayoutX(newVal.doubleValue() - 220);
            if (propertiesPanel != null) {
                propertiesPanel.setLayoutX(newVal.doubleValue() - 230);
            }
        });

        container.heightProperty().addListener((obs, old, newVal) -> {
            coordLabel.setLayoutY(newVal.doubleValue() - 30);
            if (propertiesPanel != null) {
                propertiesPanel.setLayoutY(newVal.doubleValue() - 260);
            }
        });

        updateToolButtonStyles();
    }

    private void createPropertiesPanel() {
        propertiesPanel = new VBox(10);
        propertiesPanel.setStyle("-fx-background-color: rgba(30,30,40,0.95); -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #555; -fx-border-radius: 8;");
        propertiesPanel.setLayoutX(container.getWidth() - 230);
        propertiesPanel.setLayoutY(container.getHeight() - 280);
        propertiesPanel.setPrefWidth(200);
        propertiesPanel.setVisible(false);

        Label titleLabel = new Label("СВОЙСТВА");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        propertiesPanel.getChildren().add(titleLabel);

        container.getChildren().add(propertiesPanel);
    }

    private void updatePropertiesPanel() {
        if (propertiesPanel == null || isUpdatingProperties) return;
        isUpdatingProperties = true;

        try {
            propertiesPanel.getChildren().clear();

            Label titleLabel = new Label("СВОЙСТВА");
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
            propertiesPanel.getChildren().add(titleLabel);

            if (selectedRoom != null) {
                addRoomProperties(selectedRoom);
                propertiesPanel.setVisible(true);
            } else if (selectedWall != null) {
                addWallProperties(selectedWall);
                propertiesPanel.setVisible(true);
            } else if (selectedItem != null) {
                addItemProperties(selectedItem);
                propertiesPanel.setVisible(true);
            } else {
                propertiesPanel.setVisible(false);
            }
        } finally {
            isUpdatingProperties = false;
        }
    }

    private void addRoomProperties(RoomArea room) {
        Label nameLabel = new Label("Название:");
        nameLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        TextField nameField = new TextField(room.getName());
        nameField.setStyle("-fx-background-color: #3a3a4a; -fx-text-fill: white;");
        nameField.textProperty().addListener((obs, old, newVal) -> {
            room.setName(newVal);
            draw();
        });

        Label heightLabel = new Label("Высота стен (м):");
        heightLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        Slider heightSlider = new Slider(2.0, 4.0, room.getCeilingHeight());
        heightSlider.setShowTickLabels(true);
        heightSlider.setShowTickMarks(true);
        heightSlider.setMajorTickUnit(0.5);
        heightSlider.setStyle("-fx-control-inner-background: #3a3a4a;");
        heightSlider.valueProperty().addListener((obs, old, newVal) -> {
            room.setCeilingHeight((float)newVal.doubleValue());
            for (WallSegment wall : buildingProject.getWalls()) {
                if (isWallPartOfRoom(wall, room)) {
                    wall.setHeight((float)newVal.doubleValue());
                }
            }
            draw();
        });

        propertiesPanel.getChildren().addAll(nameLabel, nameField, heightLabel, heightSlider);
    }

    private void addWallProperties(WallSegment wall) {
        Label typeLabel = new Label("Тип стены:");
        typeLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Обычная", "Дверь", "Окно");
        typeCombo.setValue(wall.getType() == WallSegment.WallType.DOOR ? "Дверь" :
                wall.getType() == WallSegment.WallType.WINDOW ? "Окно" : "Обычная");
        typeCombo.setStyle("-fx-background-color: #3a3a4a; -fx-text-fill: white;");
        typeCombo.setOnAction(e -> {
            String selected = typeCombo.getValue();
            if ("Дверь".equals(selected)) {
                wall.setType(WallSegment.WallType.DOOR);
                wall.setOpeningWidth(0.9f);
                wall.setOpeningHeight(2.0f);
            } else if ("Окно".equals(selected)) {
                wall.setType(WallSegment.WallType.WINDOW);
                wall.setOpeningWidth(1.2f);
                wall.setOpeningHeight(1.5f);
            } else {
                wall.setType(WallSegment.WallType.INNER);
            }
            draw();
        });

        Label heightLabel = new Label("Высота (м):");
        heightLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        Slider heightSlider = new Slider(2.0, 4.0, wall.getHeight());
        heightSlider.setShowTickLabels(true);
        heightSlider.valueProperty().addListener((obs, old, newVal) -> {
            wall.setHeight((float)newVal.doubleValue());
            draw();
        });

        propertiesPanel.getChildren().addAll(typeLabel, typeCombo, heightLabel, heightSlider);
    }

    private void addItemProperties(FurnitureItem item) {
        Label nameLabel = new Label("Название:");
        nameLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        TextField nameField = new TextField(item.getName());
        nameField.setStyle("-fx-background-color: #3a3a4a; -fx-text-fill: white;");
        nameField.textProperty().addListener((obs, old, newVal) -> {
            item.setName(newVal);
            draw();
        });

        Label scaleLabel = new Label("Масштаб:");
        scaleLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        Slider scaleSlider = new Slider(0.5, 2.0, item.getScale());
        scaleSlider.setShowTickLabels(true);
        scaleSlider.valueProperty().addListener((obs, old, newVal) -> {
            item.setScale((float)newVal.doubleValue());
            draw();
        });

        Label rotLabel = new Label("Поворот (градусы):");
        rotLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        Slider rotSlider = new Slider(0, 360, Math.toDegrees(item.getRotation()));
        rotSlider.setShowTickLabels(true);
        rotSlider.setShowTickMarks(true);
        rotSlider.setMajorTickUnit(90);
        rotSlider.setMinorTickCount(3);
        rotSlider.setStyle("-fx-control-inner-background: #3a3a4a;");
        rotSlider.valueProperty().addListener((obs, old, newVal) -> {
            item.setRotation((float) Math.toRadians(newVal.doubleValue()));
            draw();
        });

        Button deleteBtn = new Button("УДАЛИТЬ");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6;");
        deleteBtn.setOnAction(e -> {
            buildingProject.getFurniture().remove(item);
            selectedItem = null;
            propertiesPanel.setVisible(false);
            draw();
            if (statusLabel != null) statusLabel.setText("🗑️ Предмет удален");
        });

        propertiesPanel.getChildren().addAll(nameLabel, nameField, scaleLabel, scaleSlider, rotLabel, rotSlider, deleteBtn);
    }

    private void deleteSelected() {
        if (selectedRoom != null) {
            List<WallSegment> wallsToRemove = new ArrayList<>();
            for (WallSegment wall : buildingProject.getWalls()) {
                if (isWallPartOfRoom(wall, selectedRoom)) {
                    wallsToRemove.add(wall);
                }
            }
            buildingProject.getWalls().removeAll(wallsToRemove);
            buildingProject.getRooms().remove(selectedRoom);
            selectedRoom = null;
            if (statusLabel != null) statusLabel.setText("✅ Комната и ее стены удалены");
        } else if (selectedWall != null) {
            buildingProject.getWalls().remove(selectedWall);
            selectedWall = null;
            if (statusLabel != null) statusLabel.setText("✅ Стена удалена");
        } else if (selectedItem != null) {
            buildingProject.getFurniture().remove(selectedItem);
            selectedItem = null;
            if (statusLabel != null) statusLabel.setText("✅ Предмет удален");
        } else {
            if (statusLabel != null) statusLabel.setText("⚠️ Ничего не выбрано для удаления");
        }

        updatePropertiesPanel();
        draw();
    }

    private boolean isWallPartOfRoom(WallSegment wall, RoomArea room) {
        List<Point2DData> vertices = room.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Point2DData p1 = vertices.get(i);
            Point2DData p2 = vertices.get((i + 1) % vertices.size());

            if ((Math.abs(wall.getX1() - p1.x) < 0.1 && Math.abs(wall.getZ1() - p1.y) < 0.1 &&
                    Math.abs(wall.getX2() - p2.x) < 0.1 && Math.abs(wall.getZ2() - p2.y) < 0.1) ||
                    (Math.abs(wall.getX1() - p2.x) < 0.1 && Math.abs(wall.getZ1() - p2.y) < 0.1 &&
                            Math.abs(wall.getX2() - p1.x) < 0.1 && Math.abs(wall.getZ2() - p1.y) < 0.1)) {
                return true;
            }
        }
        return false;
    }

    private void updateRoomFromWall(WallSegment wall) {
        for (RoomArea room : buildingProject.getRooms()) {
            if (isWallPartOfRoom(wall, room)) {
                List<WallSegment> roomWalls = new ArrayList<>();
                for (WallSegment w : buildingProject.getWalls()) {
                    if (isWallPartOfRoom(w, room)) {
                        roomWalls.add(w);
                    }
                }

                if (roomWalls.size() >= 3) {
                    List<Point2DData> newVertices = new ArrayList<>();
                    WallSegment current = roomWalls.get(0);
                    newVertices.add(new Point2DData(current.getX1(), current.getZ1()));
                    newVertices.add(new Point2DData(current.getX2(), current.getZ2()));

                    boolean found = true;
                    while (found && newVertices.size() < roomWalls.size() + 1) {
                        found = false;
                        Point2DData last = newVertices.get(newVertices.size() - 1);
                        for (WallSegment w : roomWalls) {
                            if (w != current) {
                                if (Math.hypot(w.getX1() - last.x, w.getZ1() - last.y) < 0.1) {
                                    newVertices.add(new Point2DData(w.getX2(), w.getZ2()));
                                    current = w;
                                    found = true;
                                    break;
                                } else if (Math.hypot(w.getX2() - last.x, w.getZ2() - last.y) < 0.1) {
                                    newVertices.add(new Point2DData(w.getX1(), w.getZ1()));
                                    current = w;
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (newVertices.size() > 1) {
                        Point2DData first = newVertices.get(0);
                        Point2DData last = newVertices.get(newVertices.size() - 1);
                        if (Math.hypot(first.x - last.x, first.y - last.y) < 0.1) {
                            newVertices.remove(newVertices.size() - 1);
                        }
                    }

                    room.setVertices(newVertices);
                }
                break;
            }
        }
    }

    private void generateWallsFromRoom(RoomArea room) {
        List<Point2DData> vertices = room.getVertices();
        if (vertices.size() < 3) {
            if (statusLabel != null) {
                statusLabel.setText("⚠️ Комната должна иметь хотя бы 3 угла");
            }
            return;
        }

        List<WallSegment> wallsToRemove = new ArrayList<>();
        for (WallSegment wall : buildingProject.getWalls()) {
            if (isWallPartOfRoom(wall, room)) {
                wallsToRemove.add(wall);
            }
        }
        buildingProject.getWalls().removeAll(wallsToRemove);

        for (int i = 0; i < vertices.size(); i++) {
            Point2DData p1 = vertices.get(i);
            Point2DData p2 = vertices.get((i + 1) % vertices.size());

            boolean wallExists = false;
            for (WallSegment existingWall : buildingProject.getWalls()) {
                if ((Math.abs(existingWall.getX1() - p1.x) < 0.1 && Math.abs(existingWall.getZ1() - p1.y) < 0.1 &&
                        Math.abs(existingWall.getX2() - p2.x) < 0.1 && Math.abs(existingWall.getZ2() - p2.y) < 0.1) ||
                        (Math.abs(existingWall.getX1() - p2.x) < 0.1 && Math.abs(existingWall.getZ1() - p2.y) < 0.1 &&
                                Math.abs(existingWall.getX2() - p1.x) < 0.1 && Math.abs(existingWall.getZ2() - p1.y) < 0.1)) {
                    wallExists = true;
                    break;
                }
            }

            if (!wallExists) {
                WallSegment wall = new WallSegment((float)p1.x, (float)p1.y, (float)p2.x, (float)p2.y);
                wall.setHeight(room.getCeilingHeight());

                if (hasAdjacentRoom(p1, p2)) {
                    wall.setType(WallSegment.WallType.DOOR);
                    wall.setOpeningWidth(0.9f);
                    wall.setOpeningHeight(2.0f);
                    if (statusLabel != null) {
                        statusLabel.setText("🚪 Создан дверной проем");
                    }
                }

                buildingProject.getWalls().add(wall);
            }
        }

        draw();
        if (statusLabel != null) {
            statusLabel.setText("✅ Создано " + buildingProject.getWalls().size() + " стен");
        }
    }

    private boolean hasAdjacentRoom(Point2DData p1, Point2DData p2) {
        for (RoomArea room : buildingProject.getRooms()) {
            List<Point2DData> vertices = room.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                Point2DData v1 = vertices.get(i);
                Point2DData v2 = vertices.get((i + 1) % vertices.size());

                boolean match1 = (Math.abs(v1.x - p1.x) < 0.1 && Math.abs(v1.y - p1.y) < 0.1 &&
                        Math.abs(v2.x - p2.x) < 0.1 && Math.abs(v2.y - p2.y) < 0.1);
                boolean match2 = (Math.abs(v1.x - p2.x) < 0.1 && Math.abs(v1.y - p2.y) < 0.1 &&
                        Math.abs(v2.x - p1.x) < 0.1 && Math.abs(v2.y - p1.y) < 0.1);

                if (match1 || match2) {
                    return true;
                }
            }
        }
        return false;
    }

    private Button createToolButton(String text, Tool tool, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setUserData(tool);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 5;");
        btn.setOnAction(e -> {
            currentTool = tool;
            selectedFurniture = null;
            isDrawing = false;
            isDragging = false;
            draggingWall = null;
            draggedVertex = null;
            draggedWallForVertex = null;
            currentWallPoints.clear();
            currentRoomPoints.clear();
            previewRotation = 0;
            updateToolButtonStyles();
            if (statusLabel != null) {
                statusLabel.setText(getToolMessage(tool));
            }
            draw();
        });
        return btn;
    }

    private String getToolMessage(Tool tool) {
        switch (tool) {
            case ROOM: return "🏠 Комната: кликайте по углам, клик на первую точку для завершения";
            case WALL: return "📏 Стена: клик для начала, клик для завершения";
            case FURNITURE: return "🪑 Выберите мебель в библиотеке справа, клик для размещения (R/Shift - поворот)";
            case LAMP: return "💡 Выберите лампу в библиотеке справа, клик для размещения (R/Shift - поворот)";
            case SENSOR: return "📡 Выберите датчик в библиотеке справа, клик для размещения (R/Shift - поворот)";
            case SELECT: return "🔍 Режим выбора: кликните на объект для выделения, перетаскивайте углы или стены";
            case DOOR: return "🚪 Кликните на стену, чтобы добавить дверь";
            case WINDOW: return "🪟 Кликните на стену, чтобы добавить окно";
            default: return "";
        }
    }

    private void updateToolButtonStyles() {
        for (var node : toolBox.getChildren()) {
            if (node instanceof Button && ((Button) node).getUserData() instanceof Tool) {
                Button btn = (Button) node;
                Tool tool = (Tool) btn.getUserData();
                if (tool == currentTool) {
                    btn.setStyle(btn.getStyle() + "; -fx-border-color: white; -fx-border-width: 2;");
                } else {
                    btn.setStyle(btn.getStyle().replace("; -fx-border-color: white; -fx-border-width: 2;", ""));
                }
            }
        }
    }

    private String getCategoryIcon(ItemCategory category) {
        switch (category) {
            case LAMP: return "💡";
            case SENSOR: return "📡";
            case FURNITURE: return "🪑";
            default: return "📦";
        }
    }

    private void placeItem(double x, double y, Tool tool) {
        if (selectedFurniture == null) return;

        ItemLibrary lib = ItemLibrary.getInstance();
        LibraryItem libItem = lib.getItem(selectedFurniture.getId());

        if (libItem == null) return;

        float width = libItem.getWidth() * selectedFurniture.getDefaultScale();
        float depth = libItem.getDepth() * selectedFurniture.getDefaultScale();
        float rotationRad = (float) Math.toRadians(previewRotation);

        float finalX = (float) x;
        float finalZ = (float) y;
        float finalY = libItem.getDefaultYOffset();
        float finalRotation = rotationRad;
        String placementSurface = "floor";

        // Для ламп и датчиков проверяем близость к стенам
        if (tool == Tool.LAMP || tool == Tool.SENSOR) {
            WallSegment nearestWall = null;
            double minDist = 0.5;
            double hitX = x, hitZ = y;

            for (WallSegment wall : buildingProject.getWalls()) {
                double dist = pointToLineDistance(x, y, wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
                if (dist < minDist) {
                    minDist = dist;
                    nearestWall = wall;
                    double[] proj = projectPointOnSegment(x, y, wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
                    if (proj != null) {
                        hitX = proj[0];
                        hitZ = proj[1];
                    }
                    double wallAngle = Math.atan2(wall.getZ2() - wall.getZ1(), wall.getX2() - wall.getX1());
                    finalRotation = (float) wallAngle;
                }
            }

            // Если близко к стене - размещаем на стене
            if (nearestWall != null && minDist < 0.5) {
                placementSurface = "wall";
                finalX = (float) hitX;
                finalZ = (float) hitZ;
                double perpX = -Math.sin(finalRotation);
                double perpZ = Math.cos(finalRotation);
                float offset = depth / 2;
                finalX += perpX * offset;
                finalZ += perpZ * offset;
                // Высота на стене - для датчиков 1.2м, для ламп 1.5м
                if (tool == Tool.LAMP) {
                    finalY = 1.5f;
                } else {
                    finalY = 1.2f;
                }
            }
            // Иначе проверяем потолок (если предмет должен быть на потолке)
            else if (libItem.getDefaultYOffset() >= 2.0f) {
                placementSurface = "ceiling";
                finalY = libItem.getDefaultYOffset();
                // На потолке поворот не нужен
                finalRotation = 0;
            }
            // Иначе на полу
            else {
                placementSurface = "floor";
                finalY = 0;
            }
        }

        // Проверка коллизий
        boolean wallCollision = CollisionChecker.checkWallCollision(
                finalX, finalZ, width, depth, finalRotation,
                buildingProject.getWalls()
        );

        boolean furnitureCollision = CollisionChecker.checkFurnitureCollision(
                finalX, finalZ, width, depth, finalRotation,
                buildingProject.getFurniture(), null
        );

        if (wallCollision) {
            if (statusLabel != null) {
                statusLabel.setText("❌ Нельзя разместить: пересечение со стеной");
            }
            return;
        }

        if (furnitureCollision) {
            if (statusLabel != null) {
                statusLabel.setText("❌ Нельзя разместить: пересечение с мебелью");
            }
            return;
        }

        String category;

        switch (tool) {
            case LAMP:
                category = "lamp";
                break;
            case SENSOR:
                category = "sensor";
                break;
            default:
                category = "furniture";
        }

        FurnitureItem item = new FurnitureItem(
                selectedFurniture.getId(),
                selectedFurniture.getName(),
                category
        );
        item.setX(finalX);
        item.setZ(finalZ);
        item.setY(finalY);
        item.setScale(selectedFurniture.getDefaultScale());
        item.setRotation(finalRotation);
        item.setParam("placementSurface", placementSurface);

        buildingProject.getFurniture().add(item);
        draw();

        previewRotation = 0;

        String surfaceName = placementSurface.equals("wall") ? "на стене" :
                (placementSurface.equals("ceiling") ? "на потолке" : "на полу");

        if (statusLabel != null) {
            statusLabel.setText("✅ Размещено: " + selectedFurniture.getName() + " (" + surfaceName + ")");
        }
    }

    private void addOpeningToWall(double x, double y, Tool tool) {
        WallSegment nearestWall = null;
        double minDist = 0.5;
        double hitX = x, hitZ = y;

        for (WallSegment wall : buildingProject.getWalls()) {
            double dist = pointToLineDistance(x, y, wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
            if (dist < minDist) {
                minDist = dist;
                nearestWall = wall;
                double[] proj = projectPointOnSegment(x, y, wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
                if (proj != null) {
                    hitX = proj[0];
                    hitZ = proj[1];
                }
            }
        }

        if (nearestWall != null) {
            double wallLength = nearestWall.getLength();
            double startToHit = Math.hypot(hitX - nearestWall.getX1(), hitZ - nearestWall.getZ1());
            double centerPos = startToHit / wallLength;

            if (tool == Tool.DOOR) {
                nearestWall.setType(WallSegment.WallType.DOOR);
                nearestWall.setOpeningWidth(0.9f);
                nearestWall.setOpeningHeight(2.0f);
                if (statusLabel != null) statusLabel.setText(String.format("🚪 Дверь добавлена в %.0f%% стены", centerPos * 100));
            } else if (tool == Tool.WINDOW) {
                nearestWall.setType(WallSegment.WallType.WINDOW);
                nearestWall.setOpeningWidth(1.2f);
                nearestWall.setOpeningHeight(1.5f);
                if (statusLabel != null) statusLabel.setText(String.format("🪟 Окно добавлено в %.0f%% стены", centerPos * 100));
            }
            draw();
        } else {
            if (statusLabel != null) statusLabel.setText("⚠️ Кликните на стену");
        }
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
            isPanning = true;
            lastPanX = e.getX();
            lastPanY = e.getY();
            canvas.setCursor(Cursor.CLOSED_HAND);
            return;
        }

        Point2DData world = screenToWorld(e.getX(), e.getY());
        double x = world.x;
        double y = world.y;

        switch (currentTool) {
            case SELECT:
                selectObject(x, y);

                if (selectedWall != null) {
                    double distToStart = Math.hypot(x - selectedWall.getX1(), y - selectedWall.getZ1());
                    double distToEnd = Math.hypot(x - selectedWall.getX2(), y - selectedWall.getZ2());

                    if (distToStart < 0.3) {
                        draggedVertex = new Point2DData(selectedWall.getX1(), selectedWall.getZ1());
                        draggedWallForVertex = selectedWall;
                        draggedVertexIndex = 0;
                        isDragging = true;
                        if (statusLabel != null) statusLabel.setText("🔍 Перетаскивание угла стены");
                    } else if (distToEnd < 0.3) {
                        draggedVertex = new Point2DData(selectedWall.getX2(), selectedWall.getZ2());
                        draggedWallForVertex = selectedWall;
                        draggedVertexIndex = 1;
                        isDragging = true;
                        if (statusLabel != null) statusLabel.setText("🔍 Перетаскивание угла стены");
                    } else {
                        draggingWall = selectedWall;
                        dragStartX = x;
                        dragStartY = y;
                        isDragging = true;
                    }
                }
                break;

            case ROOM:
                Point2DData snapped = snapToNearestPoint(x, y);
                if (snapped != null) {
                    x = snapped.x;
                    y = snapped.y;
                    if (statusLabel != null) {
                        statusLabel.setText("🔗 Привязано к точке (" + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ")");
                    }
                }

                if (!currentRoomPoints.isEmpty()) {
                    Point2DData first = currentRoomPoints.get(0);
                    double distToFirst = Math.hypot(x - first.x, y - first.y);
                    if (distToFirst < SNAP_DISTANCE && currentRoomPoints.size() >= 2) {
                        RoomArea room = new RoomArea("Комната " + (buildingProject.getRooms().size() + 1));
                        for (Point2DData p : currentRoomPoints) {
                            room.addVertex(p.x, p.y);
                        }
                        buildingProject.getRooms().add(room);
                        generateWallsFromRoom(room);
                        currentRoomPoints.clear();
                        isDrawing = false;
                        draw();
                        if (statusLabel != null) statusLabel.setText("✅ Комната добавлена, стены созданы");
                        return;
                    }
                }

                currentRoomPoints.add(new Point2DData(x, y));
                isDrawing = true;
                draw();
                break;

            case WALL:
                if (!isDrawing) {
                    currentWallPoints.clear();
                    currentWallPoints.add(new Point2DData(x, y));
                    isDrawing = true;
                } else {
                    Point2DData start = currentWallPoints.get(0);
                    WallSegment wall = new WallSegment((float)start.x, (float)start.y, (float)x, (float)y);
                    wall.setHeight(3.0f);
                    buildingProject.getWalls().add(wall);
                    currentWallPoints.clear();
                    isDrawing = false;
                    if (statusLabel != null) statusLabel.setText("✅ Стена добавлена");
                    draw();
                }
                break;

            case FURNITURE:
            case LAMP:
            case SENSOR:
                if (selectedFurniture != null) {
                    canvas.requestFocus();
                    placeItem(x, y, currentTool);
                } else if (statusLabel != null) {
                    statusLabel.setText("⚠️ Сначала выберите предмет в библиотеке");
                }
                break;

            case DOOR:
            case WINDOW:
                addOpeningToWall(x, y, currentTool);
                break;
        }
        draw();
    }

    private void onMouseDragged(MouseEvent e) {
        if (isPanning) {
            double dx = e.getX() - lastPanX;
            double dy = e.getY() - lastPanY;

            double canvasWidth = canvas.getWidth();
            double canvasHeight = canvas.getHeight();

            double worldDeltaX = (dx / canvasWidth) * WORLD_SIZE / viewScale;
            double worldDeltaY = -(dy / canvasHeight) * WORLD_SIZE / viewScale;

            viewOffsetX -= worldDeltaX;
            viewOffsetY -= worldDeltaY;

            lastPanX = e.getX();
            lastPanY = e.getY();
            draw();
            return;
        }

        Point2DData world = screenToWorld(e.getX(), e.getY());

        if (isDragging && draggedVertex != null && draggedWallForVertex != null) {
            if (draggedVertexIndex == 0) {
                draggedWallForVertex.setX1((float)world.x);
                draggedWallForVertex.setZ1((float)world.y);
            } else {
                draggedWallForVertex.setX2((float)world.x);
                draggedWallForVertex.setZ2((float)world.y);
            }
            draw();
            updateRoomFromWall(draggedWallForVertex);
        } else if (isDragging && draggingWall != null) {
            double dx = world.x - dragStartX;
            double dy = world.y - dragStartY;

            draggingWall.setX1(draggingWall.getX1() + (float)dx);
            draggingWall.setZ1(draggingWall.getZ1() + (float)dy);
            draggingWall.setX2(draggingWall.getX2() + (float)dx);
            draggingWall.setZ2(draggingWall.getZ2() + (float)dy);

            dragStartX = world.x;
            dragStartY = world.y;
            draw();
            updateRoomFromWall(draggingWall);
        }
    }

    private void onMouseReleased(MouseEvent e) {
        if (e.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
            isPanning = false;
            canvas.setCursor(Cursor.DEFAULT);
            return;
        }

        isDragging = false;
        draggingWall = null;
        draggedVertex = null;
        draggedWallForVertex = null;
        draggedVertexIndex = -1;
    }

    private void onMouseMoved(MouseEvent e) {
        Point2DData world = screenToWorld(e.getX(), e.getY());
        mouseWorldX = world.x;
        mouseWorldY = world.y;

        if (currentTool == Tool.FURNITURE || currentTool == Tool.LAMP || currentTool == Tool.SENSOR) {
            if (selectedFurniture != null) {
                canvas.requestFocus();
            }
        }

        if ((currentTool == Tool.FURNITURE || currentTool == Tool.LAMP || currentTool == Tool.SENSOR)
                && selectedFurniture != null) {

            ItemLibrary lib = ItemLibrary.getInstance();
            LibraryItem libItem = lib.getItem(selectedFurniture.getId());

            if (libItem != null) {
                float width = libItem.getWidth() * selectedFurniture.getDefaultScale();
                float depth = libItem.getDepth() * selectedFurniture.getDefaultScale();
                float rotationRad = (float) Math.toRadians(previewRotation);

                previewX = (float) mouseWorldX;
                previewZ = (float) mouseWorldY;

                String surface = "floor";

                // Для ламп и датчиков определяем поверхность
                if (currentTool == Tool.LAMP || currentTool == Tool.SENSOR) {
                    WallSegment nearestWall = null;
                    double minDist = 0.5;

                    for (WallSegment wall : buildingProject.getWalls()) {
                        double dist = pointToLineDistance(previewX, previewZ,
                                wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
                        if (dist < minDist) {
                            minDist = dist;
                            nearestWall = wall;
                            double[] proj = projectPointOnSegment(previewX, previewZ,
                                    wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
                            if (proj != null) {
                                previewX = (float) proj[0];
                                previewZ = (float) proj[1];
                            }
                            double wallAngle = Math.atan2(wall.getZ2() - wall.getZ1(), wall.getX2() - wall.getX1());
                            rotationRad = (float) wallAngle;
                            surface = "wall";
                        }
                    }

                    if (nearestWall != null && minDist < 0.5) {
                        double perpX = -Math.sin(rotationRad);
                        double perpZ = Math.cos(rotationRad);
                        float offset = depth / 2;
                        previewX += perpX * offset;
                        previewZ += perpZ * offset;
                    } else if (libItem.getDefaultYOffset() >= 2.0f) {
                        surface = "ceiling";
                    } else {
                        surface = "floor";
                    }
                }

                isPlacementValid = !CollisionChecker.checkWallCollision(
                        previewX, previewZ, width, depth, rotationRad,
                        buildingProject.getWalls()
                ) && !CollisionChecker.checkFurnitureCollision(
                        previewX, previewZ, width, depth, rotationRad,
                        buildingProject.getFurniture(), null
                );
            }
        }

        draw();
    }

    private void selectObject(double x, double y) {
        for (FurnitureItem item : buildingProject.getFurniture()) {
            if (Math.hypot(x - item.getX(), y - item.getZ()) < 0.5) {
                selectedWall = null;
                selectedRoom = null;
                selectedItem = item;
                if (statusLabel != null) statusLabel.setText("🔍 Выбран: " + item.getName());
                updatePropertiesPanel();
                return;
            }
        }

        for (RoomArea room : buildingProject.getRooms()) {
            if (isPointInPolygon(x, y, room.getVertices())) {
                selectedWall = null;
                selectedRoom = room;
                selectedItem = null;
                if (statusLabel != null) statusLabel.setText("🔍 Выбрана комната: " + room.getName());
                updatePropertiesPanel();
                return;
            }
        }

        for (WallSegment wall : buildingProject.getWalls()) {
            double dist = pointToLineDistance(x, y, wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
            if (dist < 0.3) {
                selectedWall = wall;
                selectedRoom = null;
                selectedItem = null;
                if (statusLabel != null) statusLabel.setText("🔍 Выбрана стена");
                updatePropertiesPanel();
                return;
            }
        }

        selectedWall = null;
        selectedRoom = null;
        selectedItem = null;
        if (statusLabel != null) statusLabel.setText("🔍 Ничего не выбрано");
        updatePropertiesPanel();
    }

    private boolean isPointInPolygon(double x, double y, List<Point2DData> vertices) {
        boolean inside = false;
        for (int i = 0, j = vertices.size() - 1; i < vertices.size(); j = i++) {
            Point2DData vi = vertices.get(i);
            Point2DData vj = vertices.get(j);
            if (((vi.y > y) != (vj.y > y)) &&
                    (x < (vj.x - vi.x) * (y - vi.y) / (vj.y - vi.y) + vi.x)) {
                inside = !inside;
            }
        }
        return inside;
    }

    private double pointToLineDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len2 = C * C + D * D;
        if (len2 == 0) return Math.hypot(px - x1, py - y1);

        double param = dot / len2;
        double xx, yy;
        if (param < 0) { xx = x1; yy = y1; }
        else if (param > 1) { xx = x2; yy = y2; }
        else { xx = x1 + param * C; yy = y1 + param * D; }

        return Math.hypot(px - xx, py - yy);
    }

    private void clearAll() {
        buildingProject.getWalls().clear();
        buildingProject.getRooms().clear();
        buildingProject.getFurniture().clear();
        selectedWall = null;
        selectedRoom = null;
        selectedItem = null;
        draw();
        if (statusLabel != null) statusLabel.setText("🗑️ Все объекты удалены");
        updatePropertiesPanel();
    }

    private void draw() {
        if (gc == null || canvas == null || canvas.getWidth() == 0) return;

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(Color.rgb(40, 40, 50));
        gc.fillRect(0, 0, w, h);

        // Сетка
        gc.setStroke(Color.rgb(70, 70, 90));
        gc.setLineWidth(0.5);
        for (double x = -10; x <= 10; x += 1) {
            Point2DData p1 = worldToScreen(x, -10);
            Point2DData p2 = worldToScreen(x, 10);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }
        for (double y = -10; y <= 10; y += 1) {
            Point2DData p1 = worldToScreen(-10, y);
            Point2DData p2 = worldToScreen(10, y);
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Оси
        gc.setStroke(Color.rgb(150, 100, 100));
        gc.setLineWidth(2);
        Point2DData ox1 = worldToScreen(-10, 0);
        Point2DData ox2 = worldToScreen(10, 0);
        Point2DData oz1 = worldToScreen(0, -10);
        Point2DData oz2 = worldToScreen(0, 10);
        gc.strokeLine(ox1.x, ox1.y, ox2.x, ox2.y);
        gc.strokeLine(oz1.x, oz1.y, oz2.x, oz2.y);

        // Стены
        for (WallSegment wall : buildingProject.getWalls()) {
            Point2DData p1 = worldToScreen(wall.getX1(), wall.getZ1());
            Point2DData p2 = worldToScreen(wall.getX2(), wall.getZ2());

            if (wall == selectedWall) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(8);
            } else if (wall.getType() == WallSegment.WallType.DOOR) {
                gc.setStroke(Color.rgb(100, 200, 100));
                gc.setLineWidth(6);
                Point2DData center = worldToScreen((wall.getX1() + wall.getX2()) / 2, (wall.getZ1() + wall.getZ2()) / 2);
                gc.setFill(Color.rgb(100, 200, 100));
                gc.fillRect(center.x - 8, center.y - 8, 16, 16);
                gc.setFill(Color.WHITE);
                gc.fillText("🚪", center.x - 5, center.y + 5);
            } else if (wall.getType() == WallSegment.WallType.WINDOW) {
                gc.setStroke(Color.rgb(100, 150, 255));
                gc.setLineWidth(6);
                Point2DData center = worldToScreen((wall.getX1() + wall.getX2()) / 2, (wall.getZ1() + wall.getZ2()) / 2);
                gc.setFill(Color.rgb(100, 150, 255));
                gc.fillRect(center.x - 8, center.y - 8, 16, 16);
                gc.setFill(Color.WHITE);
                gc.fillText("🪟", center.x - 5, center.y + 5);
            } else {
                gc.setStroke(Color.rgb(200, 100, 100));
                gc.setLineWidth(6);
            }
            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Комнаты
        for (RoomArea room : buildingProject.getRooms()) {
            if (room.getVertices().size() >= 3) {
                double[] xPoints = new double[room.getVertices().size()];
                double[] yPoints = new double[room.getVertices().size()];
                for (int i = 0; i < room.getVertices().size(); i++) {
                    Point2DData p = worldToScreen(room.getVertices().get(i).x, room.getVertices().get(i).y);
                    xPoints[i] = p.x;
                    yPoints[i] = p.y;
                }

                if (room == selectedRoom) {
                    gc.setFill(Color.rgb(150, 200, 100, 0.5));
                    gc.setStroke(Color.YELLOW);
                } else {
                    gc.setFill(Color.rgb(100, 150, 200, 0.3));
                    gc.setStroke(Color.rgb(100, 150, 200));
                }
                gc.setLineWidth(2);
                gc.fillPolygon(xPoints, yPoints, room.getVertices().size());
                gc.strokePolygon(xPoints, yPoints, room.getVertices().size());

                double centerX = room.getVertices().stream().mapToDouble(p -> p.x).average().orElse(0);
                double centerY = room.getVertices().stream().mapToDouble(p -> p.y).average().orElse(0);
                Point2DData sc = worldToScreen(centerX, centerY);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(12));
                gc.fillText(room.getName(), sc.x - 25, sc.y);
            }
        }

        // Предметы
        for (FurnitureItem item : buildingProject.getFurniture()) {
            Point2DData sp = worldToScreen(item.getX(), item.getZ());
            String icon;
            Color color;

            switch (item.getCategory()) {
                case "lamp": icon = "💡"; color = Color.ORANGE; break;
                case "sensor": icon = "📡"; color = Color.CYAN; break;
                default: icon = "🪑"; color = Color.SADDLEBROWN;
            }

            if (item == selectedItem) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(3);
                gc.strokeOval(sp.x - 12, sp.y - 12, 24, 24);
            }

            gc.setFill(color);
            gc.fillOval(sp.x - 8, sp.y - 8, 16, 16);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(14));
            gc.fillText(icon, sp.x - 5, sp.y + 5);
        }

        // Превью размещения предмета с поворотом
        if ((currentTool == Tool.FURNITURE || currentTool == Tool.LAMP || currentTool == Tool.SENSOR)
                && selectedFurniture != null) {
            ItemLibrary lib = ItemLibrary.getInstance();
            LibraryItem libItem = lib.getItem(selectedFurniture.getId());

            if (libItem != null) {
                float width = libItem.getWidth() * selectedFurniture.getDefaultScale();
                float depth = libItem.getDepth() * selectedFurniture.getDefaultScale();
                float rotation = previewRotation;
                float rotationRad = (float) Math.toRadians(rotation);

                Point2DData sp = worldToScreen(previewX, previewZ);
                float screenWidth = width * 20;
                float screenDepth = depth * 20;

                // Определяем цвет в зависимости от поверхности
                Color previewColor;
                if (currentTool == Tool.LAMP || currentTool == Tool.SENSOR) {
                    boolean nearWall = false;
                    for (WallSegment wall : buildingProject.getWalls()) {
                        double dist = pointToLineDistance(previewX, previewZ,
                                wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());
                        if (dist < 0.5) {
                            nearWall = true;
                            break;
                        }
                    }

                    if (nearWall) {
                        previewColor = Color.rgb(100, 100, 255, 0.4);
                    } else if (previewZ > 2.0) {
                        previewColor = Color.rgb(255, 100, 100, 0.4);
                    } else {
                        previewColor = isPlacementValid ? Color.rgb(0, 255, 0, 0.4) : Color.rgb(255, 0, 0, 0.4);
                    }
                } else {
                    previewColor = isPlacementValid ? Color.rgb(0, 255, 0, 0.4) : Color.rgb(255, 0, 0, 0.4);
                }

                // Сохраняем текущие трансформации
                gc.save();

                // Перемещаем в центр предмета
                gc.translate(sp.x, sp.y);
                gc.rotate(rotation);

                // Рисуем прямоугольник
                gc.setFill(previewColor);
                gc.setStroke(isPlacementValid ? Color.GREEN : Color.RED);
                gc.setLineWidth(2);
                gc.fillRect(-screenWidth / 2, -screenDepth / 2, screenWidth, screenDepth);
                gc.strokeRect(-screenWidth / 2, -screenDepth / 2, screenWidth, screenDepth);

                // Рисуем стрелку направления
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(3);
                double arrowLength = screenDepth / 2 + 8;
                gc.strokeLine(0, 0, 0, arrowLength);
                gc.fillPolygon(new double[]{-6, 0, 6}, new double[]{arrowLength - 8, arrowLength, arrowLength - 8}, 3);

                // Подсказка о повороте
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(10));
                gc.fillText("R/Shift - поворот", -40, -screenDepth / 2 - 10);
                gc.fillText("Поворот: " + (int)rotation + "°", -30, -screenDepth / 2 - 20);

                // Восстанавливаем трансформации
                gc.restore();
            }
        }

        // Рисуем текущую комнату
        if (currentTool == Tool.ROOM && !currentRoomPoints.isEmpty()) {
            double[] xPoints = new double[currentRoomPoints.size() + 1];
            double[] yPoints = new double[currentRoomPoints.size() + 1];
            for (int i = 0; i < currentRoomPoints.size(); i++) {
                Point2DData p = worldToScreen(currentRoomPoints.get(i).x, currentRoomPoints.get(i).y);
                xPoints[i] = p.x;
                yPoints[i] = p.y;
            }
            Point2DData mp = worldToScreen(mouseWorldX, mouseWorldY);
            xPoints[currentRoomPoints.size()] = mp.x;
            yPoints[currentRoomPoints.size()] = mp.y;

            gc.setFill(Color.rgb(100, 200, 100, 0.4));
            gc.setStroke(Color.GREEN);
            gc.setLineWidth(2);
            gc.fillPolygon(xPoints, yPoints, currentRoomPoints.size() + 1);
            gc.strokePolygon(xPoints, yPoints, currentRoomPoints.size() + 1);
        }

        // Рисуем текущую стену
        if (currentTool == Tool.WALL && !currentWallPoints.isEmpty()) {
            Point2DData start = worldToScreen(currentWallPoints.get(0).x, currentWallPoints.get(0).y);
            Point2DData end = worldToScreen(mouseWorldX, mouseWorldY);
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(6);
            gc.strokeLine(start.x, start.y, end.x, end.y);
        }

        // Показываем точку привязки
        if (currentTool == Tool.ROOM && !currentRoomPoints.isEmpty()) {
            Point2DData snapped = snapToNearestPoint(mouseWorldX, mouseWorldY);
            if (snapped != null) {
                Point2DData screenSnapped = worldToScreen(snapped.x, snapped.y);
                gc.setFill(Color.rgb(255, 200, 0, 0.7));
                gc.fillOval(screenSnapped.x - 6, screenSnapped.y - 6, 12, 12);
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(2);
                gc.strokeOval(screenSnapped.x - 8, screenSnapped.y - 8, 16, 16);
            }
        }

        // Информация
        gc.setFill(Color.rgb(200, 200, 200));
        gc.setFont(Font.font(11));
        gc.fillText("Стен: " + buildingProject.getWalls().size() +
                " | Комнат: " + buildingProject.getRooms().size() +
                " | Предметов: " + buildingProject.getFurniture().size(), 10, h - 15);
        gc.fillText(String.format("X: %.2f, Z: %.2f | Масштаб: %.0f%%",
                mouseWorldX, mouseWorldY, viewScale * 100), w - 200, h - 15);
    }

    public BuildingProject getBuildingProject() {
        return buildingProject;
    }

    public void setBuildingProject(BuildingProject project) {
        this.buildingProject = project;
        draw();
        if (statusLabel != null) {
            statusLabel.setText("✅ Проект загружен: " + project.getName());
        }
    }

    @Override
    public void clear() {
        if (canvas != null && gc != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    @Override
    public String getName() {
        return "2D План";
    }

    @Override
    public void handleClick(double x, double y) {}
}