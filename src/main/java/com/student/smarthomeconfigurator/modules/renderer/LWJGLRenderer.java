package com.student.smarthomeconfigurator.modules.renderer;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Room;
import com.student.smarthomeconfigurator.model.Device;
import com.student.smarthomeconfigurator.devices.Lamp;
import com.student.smarthomeconfigurator.modules.simulator.DeviceSimulator;
import com.student.smarthomeconfigurator.utils.TextureManager;
import com.student.smarthomeconfigurator.utils.TextureMapper;

import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import org.joml.Math;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.assimp.*;
import org.joml.*;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.assimp.Assimp.*;

public class LWJGLRenderer implements MapRenderer {
    private Project currentProject;
    private Pane container;
    private ImageView imageView;
    private Thread renderThread;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private DeviceSimulator currentSimulator;

    private static final String CONFIG_FILE = "last_model.txt";
    private String lastModelPath = null;

    // ТОЛЬКО ТЕ МОДЕЛИ, КОТОРЫЕ ДОЛЖНЫ СВЕТИТЬСЯ (из файла Модели ламп.txt + новые)
    private static final Set<String> LAMP_MESH_NAMES = new HashSet<>(Arrays.asList(
            // С вентилятором
            "glass",
            "glass.001",
            "glass.002",
            "glass.003",
            "glass.004",
            "glass.005",
            "glass.006",
            "glass.007",
            "glass.008",
            "glass.009",
            // Лампы старые
            "st3_sg01.002",
            "st3_sg01.001",
            "st3_sg002",
            "st3_sg01",
            "pole",
            "pole.001",
            "pole.002",
            // Новые лампы
            "st3_spb01.001",
            "st3_spb002",
            "st3_spb01.002",
            "st3_spb01",
            // Торшеры
            "top",
            "body.006",
            // Панели
            "interior",
            "interior.001"
    ));

    private BlockingQueue<Runnable> glTasks = new LinkedBlockingQueue<>();

    private final ReentrantLock sceneLock = new ReentrantLock();
    private List<Mesh> meshes = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();
    private List<LampObject> lampObjects = new ArrayList<>();

    private Map<String, LampObject> lampMap = new HashMap<>();

    private float modelRotateX = 180;
    private float modelRotateY = 0;
    private float modelRotateZ = 0;
    private float modelScale = 1.0f;

    private Vector3f cameraPos = new Vector3f(8.0f, 4.0f, 15.0f);
    private Vector3f cameraTarget = new Vector3f(0.0f, 2.0f, 0.0f);
    private Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

    private float cameraYaw = 0.3f;
    private float cameraPitch = -0.2f;
    private float targetYaw = 0.3f;
    private float targetPitch = -0.2f;

    private float moveSpeed = 3.0f;
    private float mouseSensitivity = 0.005f;
    private float rotationLerpSpeed = 5.0f;
    private float zoomSpeed = 2.0f;

    private boolean moveForward = false;
    private boolean moveBackward = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean orbitMode = true;

    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean mousePressed = false;

    private long lastClickTime = 0;
    private static final long CLICK_COOLDOWN = 250;

    private long window;
    private int shaderProgram;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f modelMatrix = new Matrix4f();
    private int width = 1920, height = 1080;
    private ByteBuffer framebuffer;
    private volatile boolean needsResize = false;

    private long lastTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;
    private Label infoLabel;

    // UI для выключателей
    private VBox switchPanel;
    private boolean switchPanelVisible = false;

    private class Mesh {
        int vaoId;
        int vboId;
        int vboNormalId;
        int vboTexCoordId;
        int eboId;
        int indexCount;
        int materialIndex;
    }

    private class Material {
        String name;
        float[] diffuseColor = {0.8f, 0.8f, 0.8f, 1.0f};
        int diffuseTextureId = 0;
        boolean hasTexture = false;
        boolean isLamp = false;
    }

    private class LampObject {
        int meshIndex;
        float[] position;
        boolean isOn;
        String name;
        String type; // "spotlight", "panel", "fan", "floor", "bulb"
        float intensity;
        float targetIntensity;
        float lightRadius; // Радиус освещения для ограничения
        Button switchButton;

        public LampObject(int meshIndex, float[] position, String name, String type) {
            this.meshIndex = meshIndex;
            this.position = position;
            this.name = name;
            this.type = type;
            this.isOn = true;
            this.intensity = 1.0f;
            this.targetIntensity = 1.0f;

            // Разные радиусы для разных типов ламп
            switch (type) {
                case "spotlight":
                    this.lightRadius = 5.0f;
                    break;
                case "panel":
                    this.lightRadius = 4.0f;
                    break;
                case "fan":
                    this.lightRadius = 6.0f;
                    break;
                case "floor":
                    this.lightRadius = 3.0f;
                    break;
                case "bulb":
                    this.lightRadius = 2.0f;
                    break;
                default:
                    this.lightRadius = 4.0f;
            }
        }

        public void toggle() {
            isOn = !isOn;
            targetIntensity = isOn ? 1.0f : 0.0f;
            System.out.println("Лампа " + type + " " + name + " " + (isOn ? "включена" : "выключена"));

            if (switchButton != null) {
                Platform.runLater(() -> {
                    if (isOn) {
                        switchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        switchButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                });
            }
        }

        public void updateAnimation(double deltaTime) {
            float speed = 5.0f;
            if (intensity < targetIntensity) {
                intensity = Math.min(targetIntensity, intensity + (float)deltaTime * speed);
            } else if (intensity > targetIntensity) {
                intensity = Math.max(targetIntensity, intensity - (float)deltaTime * speed);
            }
        }

        public float[] getLightColor() {
            if (intensity <= 0.01f) return new float[]{0, 0, 0};

            switch (type) {
                case "spotlight":
                    return new float[]{1.0f * intensity, 0.95f * intensity, 0.8f * intensity};
                case "panel":
                    return new float[]{0.95f * intensity, 0.95f * intensity, 1.0f * intensity};
                case "fan":
                    return new float[]{0.9f * intensity, 1.0f * intensity, 0.9f * intensity};
                case "floor":
                    return new float[]{1.0f * intensity, 0.8f * intensity, 0.6f * intensity};
                case "bulb":
                    return new float[]{1.0f * intensity, 1.0f * intensity, 0.8f * intensity};
                default:
                    return new float[]{1.0f * intensity, 0.95f * intensity, 0.8f * intensity};
            }
        }

        public float[] getEmissiveColor() {
            if (intensity <= 0.01f) return new float[]{0, 0, 0};

            float[] col = getLightColor();
            return new float[]{col[0] * 2.0f, col[1] * 2.0f, col[2] * 2.0f};
        }

        public float getLightRadius() {
            return lightRadius;
        }
    }

    @Override
    public void render(Project project, Pane container) {
        this.currentProject = project;
        this.container = container;

        loadLastModelPath();

        imageView = new ImageView();
        imageView.setFitWidth(container.getWidth());
        imageView.setFitHeight(container.getHeight());
        imageView.setPreserveRatio(false);

        Platform.runLater(() -> {
            container.getChildren().clear();
            container.getChildren().add(imageView);
            createUI();

            container.widthProperty().addListener((obs, oldVal, newVal) -> {
                width = newVal.intValue();
                needsResize = true;
            });

            container.heightProperty().addListener((obs, oldVal, newVal) -> {
                height = newVal.intValue();
                needsResize = true;
            });

            width = (int) container.getWidth();
            height = (int) container.getHeight();
            needsResize = true;

            setupInputHandlers();
        });

        isRunning.set(true);
        renderThread = new Thread(this::runGLThread);
        renderThread.setDaemon(true);
        renderThread.start();
    }

    public void setSimulator(DeviceSimulator simulator) {
        this.currentSimulator = simulator;
    }

    private void setupInputHandlers() {
        container.setFocusTraversable(true);
        container.requestFocus();

        container.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W: moveForward = true; break;
                case S: moveBackward = true; break;
                case A: moveLeft = true; break;
                case D: moveRight = true; break;
                case Q: moveUp = true; break;
                case E: moveDown = true; break;
                case R: resetCamera(); break;
                case O:
                    orbitMode = !orbitMode;
                    break;
                case P:
                    toggleSwitchPanel();
                    break;
            }
        });

        container.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W: moveForward = false; break;
                case S: moveBackward = false; break;
                case A: moveLeft = false; break;
                case D: moveRight = false; break;
                case Q: moveUp = false; break;
                case E: moveDown = false; break;
            }
        });

        container.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mousePressed = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                container.requestFocus();
            }
        });

        container.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mousePressed = false;
            }
        });

        container.setOnMouseDragged(e -> {
            if (mousePressed) {
                double dx = e.getX() - lastMouseX;
                double dy = e.getY() - lastMouseY;

                targetYaw += dx * mouseSensitivity;
                targetPitch += dy * mouseSensitivity;
                targetPitch = (float) Math.max(-1.4, Math.min(1.4, targetPitch));

                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        container.setOnScroll(e -> {
            if (orbitMode) {
                Vector3f direction = new Vector3f(cameraTarget).sub(cameraPos).normalize();
                float delta = (float)e.getDeltaY() * zoomSpeed * 0.1f;
                cameraPos.add(direction.mul(delta));
            } else {
                Vector3f forward = new Vector3f(cameraTarget).sub(cameraPos).normalize();
                float delta = (float)e.getDeltaY() * zoomSpeed * 0.1f;
                cameraPos.add(forward.mul(delta));
                cameraTarget.add(forward.mul(delta));
            }
        });

        container.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime > CLICK_COOLDOWN) {
                    lastClickTime = now;
                    handleClick(e.getX(), e.getY());
                }
            }
        });
    }

    private void toggleSwitchPanel() {
        Platform.runLater(() -> {
            if (switchPanel == null) {
                createSwitchPanel();
            }
            switchPanelVisible = !switchPanelVisible;
            switchPanel.setVisible(switchPanelVisible);
            switchPanel.setManaged(switchPanelVisible);
        });
    }

    private void createSwitchPanel() {
        switchPanel = new VBox(5);
        switchPanel.setStyle("-fx-background-color: rgba(30,30,30,0.95); -fx-padding: 10; -fx-background-radius: 5;");
        switchPanel.setLayoutX(10);
        switchPanel.setLayoutY(200);
        switchPanel.setPrefWidth(280);
        switchPanel.setMaxHeight(500);

        Label titleLabel = new Label("РУЧНОЕ УПРАВЛЕНИЕ ЛАМПАМИ");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5;");
        switchPanel.getChildren().add(titleLabel);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        VBox switchesBox = new VBox(5);

        // Группируем лампы по типу
        Map<String, List<LampObject>> lampsByType = new LinkedHashMap<>();
        lampsByType.put("СПОТЫ", new ArrayList<>());
        lampsByType.put("ВЕНТИЛЯТОРЫ", new ArrayList<>());
        lampsByType.put("ТОРШЕРЫ", new ArrayList<>());
        lampsByType.put("ПАНЕЛИ", new ArrayList<>());
        lampsByType.put("ЛАМПОЧКИ", new ArrayList<>());

        sceneLock.lock();
        try {
            for (LampObject lamp : lampObjects) {
                switch (lamp.type) {
                    case "spotlight":
                        lampsByType.get("СПОТЫ").add(lamp);
                        break;
                    case "fan":
                        lampsByType.get("ВЕНТИЛЯТОРЫ").add(lamp);
                        break;
                    case "floor":
                        lampsByType.get("ТОРШЕРЫ").add(lamp);
                        break;
                    case "panel":
                        lampsByType.get("ПАНЕЛИ").add(lamp);
                        break;
                    case "bulb":
                        lampsByType.get("ЛАМПОЧКИ").add(lamp);
                        break;
                }
            }
        } finally {
            sceneLock.unlock();
        }

        // Создаем кнопки для каждой группы
        for (Map.Entry<String, List<LampObject>> entry : lampsByType.entrySet()) {
            String groupName = entry.getKey();
            List<LampObject> groupLamps = entry.getValue();

            if (!groupLamps.isEmpty()) {
                Label groupLabel = new Label(groupName + " (" + groupLamps.size() + ")");
                groupLabel.setStyle("-fx-text-fill: #ffaa00; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 0 0 0;");
                switchesBox.getChildren().add(groupLabel);

                for (LampObject lamp : groupLamps) {
                    String displayName = lamp.name;
                    if (displayName.length() > 35) {
                        displayName = displayName.substring(0, 32) + "...";
                    }

                    Button switchBtn = new Button("💡 " + displayName);
                    switchBtn.setMaxWidth(Double.MAX_VALUE);
                    switchBtn.setStyle(lamp.isOn ?
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-alignment: center-left; -fx-font-size: 11px; -fx-padding: 8;" :
                            "-fx-background-color: #f44336; -fx-text-fill: white; -fx-alignment: center-left; -fx-font-size: 11px; -fx-padding: 8;");
                    switchBtn.setUserData(lamp);

                    switchBtn.setOnAction(e -> {
                        LampObject targetLamp = (LampObject) switchBtn.getUserData();
                        targetLamp.toggle();
                    });

                    lamp.switchButton = switchBtn;
                    switchesBox.getChildren().add(switchBtn);
                }
            }
        }

        scrollPane.setContent(switchesBox);
        switchPanel.getChildren().add(scrollPane);

        Button closeBtn = new Button("ЗАКРЫТЬ");
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        closeBtn.setOnAction(e -> toggleSwitchPanel());
        switchPanel.getChildren().add(closeBtn);

        container.getChildren().add(switchPanel);
    }

    private void createUI() {
        infoLabel = new Label("3D");
        infoLabel.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: #00ff00; -fx-padding: 10; -fx-font-size: 13px; -fx-background-radius: 5;");
        infoLabel.setLayoutX(10);
        infoLabel.setLayoutY(10);
        container.getChildren().add(infoLabel);

        Button loadModelBtn = new Button("ЗАГРУЗИТЬ");
        loadModelBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 3;");
        loadModelBtn.setLayoutX(10);
        loadModelBtn.setLayoutY(50);
        loadModelBtn.setOnAction(e -> loadModelDialog());
        container.getChildren().add(loadModelBtn);

        Button resetCamBtn = new Button("СБРОС");
        resetCamBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 3;");
        resetCamBtn.setLayoutX(100);
        resetCamBtn.setLayoutY(50);
        resetCamBtn.setOnAction(e -> resetCamera());
        container.getChildren().add(resetCamBtn);

        Button deleteModelBtn = new Button("УДАЛИТЬ");
        deleteModelBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 3;");
        deleteModelBtn.setLayoutX(180);
        deleteModelBtn.setLayoutY(50);
        deleteModelBtn.setOnAction(e -> deleteHouse());
        container.getChildren().add(deleteModelBtn);

        Button applyTexturesBtn = new Button("ТЕКСТУРЫ");
        applyTexturesBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 3;");
        applyTexturesBtn.setLayoutX(260);
        applyTexturesBtn.setLayoutY(50);
        applyTexturesBtn.setOnAction(e -> applyTexturesToMaterials());
        container.getChildren().add(applyTexturesBtn);

        Button manualControlBtn = new Button("РУЧНОЕ УПРАВЛЕНИЕ (P)");
        manualControlBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
        manualControlBtn.setLayoutX(340);
        manualControlBtn.setLayoutY(50);
        manualControlBtn.setOnAction(e -> toggleSwitchPanel());
        container.getChildren().add(manualControlBtn);

        addRotationControls();
        addInstructions();
    }

    private void addRotationControls() {
        Label rotLabel = new Label("Поворот модели:");
        rotLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        rotLabel.setLayoutX(10);
        rotLabel.setLayoutY(90);
        container.getChildren().add(rotLabel);

        Button rotXPlus = new Button("X+");
        rotXPlus.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #FF4444; -fx-text-fill: white; -fx-background-radius: 3;");
        rotXPlus.setLayoutX(10);
        rotXPlus.setLayoutY(110);
        rotXPlus.setOnAction(e -> { modelRotateX += 15; updateModelMatrix(); });
        container.getChildren().add(rotXPlus);

        Button rotXMinus = new Button("X-");
        rotXMinus.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #FF4444; -fx-text-fill: white; -fx-background-radius: 3;");
        rotXMinus.setLayoutX(50);
        rotXMinus.setLayoutY(110);
        rotXMinus.setOnAction(e -> { modelRotateX -= 15; updateModelMatrix(); });
        container.getChildren().add(rotXMinus);

        Button rotYPlus = new Button("Y+");
        rotYPlus.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #44FF44; -fx-text-fill: black; -fx-background-radius: 3;");
        rotYPlus.setLayoutX(90);
        rotYPlus.setLayoutY(110);
        rotYPlus.setOnAction(e -> { modelRotateY += 15; updateModelMatrix(); });
        container.getChildren().add(rotYPlus);

        Button rotYMinus = new Button("Y-");
        rotYMinus.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #44FF44; -fx-text-fill: black; -fx-background-radius: 3;");
        rotYMinus.setLayoutX(130);
        rotYMinus.setLayoutY(110);
        rotYMinus.setOnAction(e -> { modelRotateY -= 15; updateModelMatrix(); });
        container.getChildren().add(rotYMinus);

        Button rotZPlus = new Button("Z+");
        rotZPlus.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #4444FF; -fx-text-fill: white; -fx-background-radius: 3;");
        rotZPlus.setLayoutX(170);
        rotZPlus.setLayoutY(110);
        rotZPlus.setOnAction(e -> { modelRotateZ += 15; updateModelMatrix(); });
        container.getChildren().add(rotZPlus);

        Button rotZMinus = new Button("Z-");
        rotZMinus.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #4444FF; -fx-text-fill: white; -fx-background-radius: 3;");
        rotZMinus.setLayoutX(210);
        rotZMinus.setLayoutY(110);
        rotZMinus.setOnAction(e -> { modelRotateZ -= 15; updateModelMatrix(); });
        container.getChildren().add(rotZMinus);

        Button resetModelBtn = new Button("СБРОС");
        resetModelBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3 8; -fx-background-color: #AAAAAA; -fx-text-fill: black; -fx-background-radius: 3;");
        resetModelBtn.setLayoutX(250);
        resetModelBtn.setLayoutY(110);
        resetModelBtn.setOnAction(e -> {
            modelRotateX = 180;
            modelRotateY = 0;
            modelRotateZ = 0;
            modelScale = 1.0f;
            updateModelMatrix();
        });
        container.getChildren().add(resetModelBtn);
    }

    private void addInstructions() {
        Label controlsLabel = new Label(
                "УПРАВЛЕНИЕ:\n" +
                        "WASD - ходить по дому\n" +
                        "Q/E - вверх/вниз\n" +
                        "ЛКМ + движение - осмотреться\n" +
                        "Колесико - зум\n" +
                        "O - орбита/ходьба\n" +
                        "R - сброс камеры\n" +
                        "P - ручное управление лампами"
        );
        controlsLabel.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: #ffaa00; -fx-padding: 8; -fx-font-size: 11px; -fx-background-radius: 3;");
        controlsLabel.setLayoutX(10);
        controlsLabel.setLayoutY(150);
        controlsLabel.setPrefWidth(220);
        controlsLabel.setWrapText(true);
        container.getChildren().add(controlsLabel);
    }

    private void updateModelMatrix() {
        modelMatrix.identity()
                .rotateX((float)java.lang.Math.toRadians(modelRotateX))
                .rotateY((float)java.lang.Math.toRadians(modelRotateY))
                .rotateZ((float)java.lang.Math.toRadians(modelRotateZ))
                .scale(modelScale);
    }

    private void resetCamera() {
        cameraPos.set(8.0f, 4.0f, 15.0f);
        cameraTarget.set(0.0f, 2.0f, 0.0f);
        cameraYaw = 0.3f;
        cameraPitch = -0.2f;
        targetYaw = 0.3f;
        targetPitch = -0.2f;
        orbitMode = true;
    }

    private void updateMovement(double deltaTime) {
        cameraYaw += (targetYaw - cameraYaw) * rotationLerpSpeed * (float)deltaTime;
        cameraPitch += (targetPitch - cameraPitch) * rotationLerpSpeed * (float)deltaTime;

        float cosPitch = (float)java.lang.Math.cos(cameraPitch);
        float sinPitch = (float)java.lang.Math.sin(cameraPitch);
        float cosYaw = (float)java.lang.Math.cos(cameraYaw);
        float sinYaw = (float)java.lang.Math.sin(cameraYaw);

        Vector3f forward = new Vector3f(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
        Vector3f right = new Vector3f(cosYaw, 0, -sinYaw);
        Vector3f up = new Vector3f(0, 1, 0);

        float speed = moveSpeed * (float)deltaTime;

        if (orbitMode) {
            if (moveForward || moveBackward || moveLeft || moveRight || moveUp || moveDown) {
                orbitMode = false;
            }

            Vector3f toTarget = new Vector3f(cameraTarget).sub(cameraPos);
            float distance = toTarget.length();

            float newX = cameraTarget.x + distance * sinYaw * cosPitch;
            float newY = cameraTarget.y + distance * sinPitch;
            float newZ = cameraTarget.z + distance * cosYaw * cosPitch;

            cameraPos.set(newX, newY, newZ);
        } else {
            if (moveForward) {
                cameraPos.add(forward.mul(speed));
                cameraTarget.add(forward.mul(speed));
            }
            if (moveBackward) {
                cameraPos.sub(forward.mul(speed));
                cameraTarget.sub(forward.mul(speed));
            }
            if (moveLeft) {
                cameraPos.sub(right.mul(speed));
                cameraTarget.sub(right.mul(speed));
            }
            if (moveRight) {
                cameraPos.add(right.mul(speed));
                cameraTarget.add(right.mul(speed));
            }
            if (moveUp) {
                cameraPos.add(up.mul(speed));
                cameraTarget.add(up.mul(speed));
            }
            if (moveDown) {
                cameraPos.sub(up.mul(speed));
                cameraTarget.sub(up.mul(speed));
            }

            cameraTarget.set(cameraPos.x + forward.x, cameraPos.y + forward.y, cameraPos.z + forward.z);
        }
    }

    private void resizeFramebuffer() {
        if (width > 0 && height > 0) {
            if (framebuffer != null) {
                memFree(framebuffer);
            }
            framebuffer = memAlloc(width * height * 4);

            if (projectionMatrix != null) {
                projectionMatrix = new Matrix4f()
                        .perspective((float)java.lang.Math.toRadians(60.0f), (float)width/height, 0.1f, 1000.0f);
            }
            needsResize = false;
        }
    }

    private void runGLThread() {
        try {
            initGLFW();
            initOpenGL();
            initShaders();

            resetCamera();
            resizeFramebuffer();

            if (lastModelPath != null) {
                File lastModel = new File(lastModelPath);
                if (lastModel.exists()) {
                    glTasks.add(() -> {
                        deleteHouseGL();
                        loadOBJFile(lastModel);
                    });
                }
            }

            long lastUpdate = System.nanoTime();

            while (isRunning.get() && !glfwWindowShouldClose(window)) {
                long now = System.nanoTime();
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                updateMovement(deltaTime);

                sceneLock.lock();
                try {
                    for (LampObject lamp : lampObjects) {
                        lamp.updateAnimation(deltaTime);
                    }
                } finally {
                    sceneLock.unlock();
                }

                if (needsResize) {
                    resizeFramebuffer();
                }

                Runnable task;
                while ((task = glTasks.poll()) != null) {
                    task.run();
                }

                render();

                frames++;
                long currentTime = System.nanoTime();
                if (currentTime - lastTime >= 1_000_000_000) {
                    fps = frames;
                    frames = 0;
                    lastTime = currentTime;

                    int texCount = 0;
                    int lampsOn = 0;
                    sceneLock.lock();
                    try {
                        for (Material mat : materials) {
                            if (mat.hasTexture) texCount++;
                            if (mat.isLamp) lampsOn++;
                        }
                    } finally {
                        sceneLock.unlock();
                    }

                    final int texCountFinal = texCount;
                    final int fpsFinal = fps;
                    final int meshesFinal = meshes.size();
                    final int lampsFound = lampObjects.size();
                    final int lampsOnFinal = lampsOn;
                    final String mode = orbitMode ? "Орбита" : "Ходьба";

                    Platform.runLater(() -> {
                        if (infoLabel != null) {
                            infoLabel.setText(String.format("FPS: %d | %s | Мешей: %d | Ламп: %d/%d | Текстур: %d",
                                    fpsFinal, mode, meshesFinal, lampsOnFinal, lampsFound, texCountFinal));
                        }
                    });
                }

                if (framebuffer != null && width > 0 && height > 0) {
                    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, framebuffer);

                    Platform.runLater(() -> {
                        try {
                            WritableImage img = new WritableImage(width, height);
                            PixelWriter pw = img.getPixelWriter();
                            pw.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(),
                                    framebuffer, width * 4);
                            imageView.setImage(img);
                        } catch (Exception ex) {
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void initGLFW() {
        if (!glfwInit()) {
            throw new IllegalStateException("Не могу инициализировать GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, "Offscreen", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Не могу создать окно GLFW");
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
    }

    private void initOpenGL() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        projectionMatrix = new Matrix4f()
                .perspective((float)java.lang.Math.toRadians(60.0f), (float)Math.max(width, 1)/Math.max(height, 1), 0.1f, 1000.0f);
        viewMatrix = new Matrix4f();
        updateModelMatrix();
    }

    private void initShaders() {
        String vertexShaderSource =
                "#version 330 core\n" +
                        "layout (location = 0) in vec3 aPos;\n" +
                        "layout (location = 1) in vec3 aNormal;\n" +
                        "layout (location = 2) in vec2 aTexCoord;\n" +
                        "out vec3 FragPos;\n" +
                        "out vec3 Normal;\n" +
                        "out vec2 TexCoord;\n" +
                        "uniform mat4 projection;\n" +
                        "uniform mat4 view;\n" +
                        "uniform mat4 model;\n" +
                        "void main() {\n" +
                        "    FragPos = vec3(model * vec4(aPos, 1.0));\n" +
                        "    Normal = mat3(transpose(inverse(model))) * aNormal;\n" +
                        "    TexCoord = aTexCoord;\n" +
                        "    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                        "}\n";

        String fragmentShaderSource =
                "#version 330 core\n" +
                        "out vec4 FragColor;\n" +
                        "in vec3 FragPos;\n" +
                        "in vec3 Normal;\n" +
                        "in vec2 TexCoord;\n" +
                        "uniform sampler2D texture1;\n" +
                        "uniform bool hasTexture;\n" +
                        "uniform vec3 diffuseColor;\n" +
                        "uniform vec3 emissiveColor;\n" +
                        "uniform vec3 ambientLight;\n" +
                        "uniform int lightCount;\n" +
                        "uniform vec3 lightPositions[32];\n" +
                        "uniform vec3 lightColors[32];\n" +
                        "uniform float lightRadii[32];\n" +
                        "void main() {\n" +
                        "    vec3 norm = normalize(Normal);\n" +
                        "    vec3 result = ambientLight * diffuseColor;\n" +
                        "    \n" +
                        "    if (lightCount > 0) {\n" +
                        "        for (int i = 0; i < lightCount; i++) {\n" +
                        "            vec3 lightDir = normalize(lightPositions[i] - FragPos);\n" +
                        "            float diff = max(dot(norm, lightDir), 0.0);\n" +
                        "            float distance = length(lightPositions[i] - FragPos);\n" +
                        "            \n" +
                        "            // Ограничиваем радиус освещения\n" +
                        "            if (distance < lightRadii[i]) {\n" +
                        "                float attenuation = 1.0 - (distance / lightRadii[i]);\n" +
                        "                attenuation = attenuation * attenuation;\n" +
                        "                vec3 diffuse = diff * lightColors[i] * attenuation * 2.0;\n" +
                        "                result += diffuse * diffuseColor;\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    result += emissiveColor;\n" +
                        "    \n" +
                        "    if (hasTexture) {\n" +
                        "        vec4 texColor = texture(texture1, TexCoord);\n" +
                        "        FragColor = texColor * vec4(result, 1.0);\n" +
                        "    } else {\n" +
                        "        FragColor = vec4(result, 1.0);\n" +
                        "    }\n" +
                        "}\n";

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private void loadOBJFile(File file) {
        saveLastModelPath(file.getAbsolutePath());

        int flags = aiProcess_Triangulate | aiProcess_FlipUVs | aiProcess_GenNormals |
                aiProcess_JoinIdenticalVertices | aiProcess_OptimizeMeshes;

        AIScene scene = aiImportFile(file.getAbsolutePath(), flags);
        if (scene == null) return;

        String basePath = file.getParent();

        List<Material> newMaterials = new ArrayList<>();
        if (scene.mNumMaterials() > 0) {
            loadMaterials(scene, basePath, newMaterials);
        }

        int numMeshes = scene.mNumMeshes();
        PointerBuffer meshesBuffer = scene.mMeshes();

        List<Mesh> newMeshes = new ArrayList<>();
        List<LampObject> newLamps = new ArrayList<>();
        lampMap.clear();

        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(meshesBuffer.get(i));
            processMesh(aiMesh, i, newMeshes, newLamps, newMaterials);
        }

        sceneLock.lock();
        try {
            deleteHouseGL();
            meshes = newMeshes;
            materials = newMaterials;
            lampObjects = newLamps;
        } finally {
            sceneLock.unlock();
        }

        aiReleaseImport(scene);
        applyTexturesToMaterials();

        System.out.println("Загружено ламп: " + lampObjects.size());
        int fanCount = 0, spotlightCount = 0, floorCount = 0, panelCount = 0, bulbCount = 0;
        for (LampObject lamp : lampObjects) {
            switch (lamp.type) {
                case "fan": fanCount++; break;
                case "spotlight": spotlightCount++; break;
                case "floor": floorCount++; break;
                case "panel": panelCount++; break;
                case "bulb": bulbCount++; break;
            }
        }
        System.out.println("Из них: вентиляторов=" + fanCount + ", спотов=" + spotlightCount +
                ", торшеров=" + floorCount + ", панелей=" + panelCount + ", лампочек=" + bulbCount);
    }

    private void loadMaterials(AIScene scene, String basePath, List<Material> newMaterials) {
        PointerBuffer materialsBuffer = scene.mMaterials();

        for (int i = 0; i < scene.mNumMaterials(); i++) {
            AIMaterial aiMaterial = AIMaterial.create(materialsBuffer.get(i));
            Material material = new Material();

            AIString name = AIString.calloc();
            if (aiGetMaterialString(aiMaterial, AI_MATKEY_NAME, aiTextureType_NONE, 0, name) == 0) {
                material.name = name.dataString();
            } else {
                material.name = "material_" + i;
            }
            name.free();

            AIColor4D color = AIColor4D.calloc();
            if (aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color) == 0) {
                material.diffuseColor[0] = color.r();
                material.diffuseColor[1] = color.g();
                material.diffuseColor[2] = color.b();
            }
            color.free();

            newMaterials.add(material);
        }
    }

    private String determineLampType(String meshName) {
        String lowerName = meshName.toLowerCase();

        if (lowerName.contains("glass")) {
            return "fan"; // лампы с вентилятором
        } else if (lowerName.contains("st3_sg") || lowerName.contains("st3_spb") || lowerName.contains("pole")) {
            return "bulb"; // обычные лампы
        } else if (lowerName.contains("top") || lowerName.contains("body")) {
            return "floor"; // торшеры
        } else if (lowerName.contains("interior")) {
            return "panel"; // панели
        } else {
            return "bulb";
        }
    }

    private void processMesh(AIMesh aiMesh, int meshIndex, List<Mesh> newMeshes, List<LampObject> newLamps, List<Material> newMaterials) {
        Mesh mesh = new Mesh();
        mesh.materialIndex = aiMesh.mMaterialIndex();

        // Получаем имя меша
        String meshName = "";
        AIString aiMeshName = aiMesh.mName();
        if (aiMeshName != null && aiMeshName.dataString() != null && !aiMeshName.dataString().isEmpty()) {
            meshName = aiMeshName.dataString();
        }

        AIVector3D.Buffer vertices = aiMesh.mVertices();
        AIVector3D.Buffer normals = aiMesh.mNormals();
        AIVector3D.Buffer texCoords = aiMesh.mTextureCoords(0);

        int numVertices = vertices.remaining();

        FloatBuffer verticesBuffer = memAllocFloat(numVertices * 3);
        FloatBuffer normalsBuffer = null;
        FloatBuffer texCoordsBuffer = null;

        if (normals != null) normalsBuffer = memAllocFloat(numVertices * 3);
        if (texCoords != null) texCoordsBuffer = memAllocFloat(numVertices * 2);

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < numVertices; i++) {
            AIVector3D v = vertices.get(i);
            verticesBuffer.put(v.x()).put(v.y()).put(v.z());

            if (normals != null && i < normals.remaining()) {
                AIVector3D n = normals.get(i);
                normalsBuffer.put(n.x()).put(n.y()).put(n.z());
            }

            if (texCoords != null && i < texCoords.remaining()) {
                AIVector3D t = texCoords.get(i);
                texCoordsBuffer.put(t.x()).put(t.y());
            }

            minX = Math.min(minX, v.x());
            maxX = Math.max(maxX, v.x());
            minY = Math.min(minY, v.y());
            maxY = Math.max(maxY, v.y());
            minZ = Math.min(minZ, v.z());
            maxZ = Math.max(maxZ, v.z());
        }

        verticesBuffer.flip();
        if (normalsBuffer != null) normalsBuffer.flip();
        if (texCoordsBuffer != null) texCoordsBuffer.flip();

        AIFace.Buffer faces = aiMesh.mFaces();
        int numFaces = faces.remaining();
        IntBuffer indicesBuffer = memAllocInt(numFaces * 3);

        for (int i = 0; i < numFaces; i++) {
            AIFace face = faces.get(i);
            IntBuffer faceIndices = face.mIndices();
            indicesBuffer.put(faceIndices.get(0));
            indicesBuffer.put(faceIndices.get(1));
            indicesBuffer.put(faceIndices.get(2));
        }

        indicesBuffer.flip();
        mesh.indexCount = indicesBuffer.remaining();

        mesh.vaoId = glGenVertexArrays();
        glBindVertexArray(mesh.vaoId);

        mesh.vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mesh.vboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        if (normalsBuffer != null) {
            mesh.vboNormalId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, mesh.vboNormalId);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);
        }

        if (texCoordsBuffer != null) {
            mesh.vboTexCoordId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, mesh.vboTexCoordId);
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
        }

        mesh.eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        memFree(verticesBuffer);
        if (normalsBuffer != null) memFree(normalsBuffer);
        if (texCoordsBuffer != null) memFree(texCoordsBuffer);
        memFree(indicesBuffer);

        newMeshes.add(mesh);

        // ТОЛЬКО ТЕ МОДЕЛИ, КОТОРЫЕ В СПИСКЕ
        boolean isLamp = false;
        String type = "bulb";

        if (!meshName.isEmpty() && LAMP_MESH_NAMES.contains(meshName)) {
            isLamp = true;
            type = determineLampType(meshName);
        }

        if (isLamp) {
            String key = meshName;

            if (!lampMap.containsKey(key)) {
                lampMap.put(key, null);

                float centerX = (minX + maxX) / 2;
                float centerY = (minY + maxY) / 2;
                float centerZ = (minZ + maxZ) / 2;

                LampObject lamp = new LampObject(newMeshes.size() - 1,
                        new float[]{centerX, centerY, centerZ},
                        meshName,
                        type);
                newLamps.add(lamp);
                System.out.println("✅ Добавлена лампа: " + meshName + " (тип: " + type + ")");

                if (mesh.materialIndex >= 0 && mesh.materialIndex < newMaterials.size()) {
                    newMaterials.get(mesh.materialIndex).isLamp = true;
                }
            }
        }
    }

    private void render() {
        if (width <= 0 || height <= 0) return;

        glViewport(0, 0, width, height);
        glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);

        viewMatrix.identity().lookAt(cameraPos, cameraTarget, cameraUp);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(16);

            projectionMatrix.get(matrixBuffer);
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "projection"), false, matrixBuffer);

            viewMatrix.get(matrixBuffer);
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "view"), false, matrixBuffer);

            glUniform3f(glGetUniformLocation(shaderProgram, "ambientLight"), 0.4f, 0.4f, 0.4f);

            List<float[]> lightPositions = new ArrayList<>();
            List<float[]> lightColors = new ArrayList<>();
            List<Float> lightRadii = new ArrayList<>();

            sceneLock.lock();
            try {
                for (LampObject lamp : lampObjects) {
                    if (lamp.isOn) {
                        lightPositions.add(lamp.position);
                        lightColors.add(lamp.getLightColor());
                        lightRadii.add(lamp.getLightRadius());
                    }
                }
            } finally {
                sceneLock.unlock();
            }

            int lightCount = Math.min(lightPositions.size(), 32);
            glUniform1i(glGetUniformLocation(shaderProgram, "lightCount"), lightCount);

            for (int i = 0; i < lightCount; i++) {
                float[] pos = lightPositions.get(i);
                float[] col = lightColors.get(i);
                float rad = lightRadii.get(i);

                int posLoc = glGetUniformLocation(shaderProgram, "lightPositions[" + i + "]");
                int colLoc = glGetUniformLocation(shaderProgram, "lightColors[" + i + "]");
                int radLoc = glGetUniformLocation(shaderProgram, "lightRadii[" + i + "]");

                if (posLoc >= 0) glUniform3f(posLoc, pos[0], pos[1], pos[2]);
                if (colLoc >= 0) glUniform3f(colLoc, col[0], col[1], col[2]);
                if (radLoc >= 0) glUniform1f(radLoc, rad);
            }

            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, modelMatrix.get(matrixBuffer));

            sceneLock.lock();
            try {
                for (int i = 0; i < meshes.size(); i++) {
                    Mesh mesh = meshes.get(i);

                    float[] emissiveColor = new float[]{0, 0, 0};
                    for (LampObject lamp : lampObjects) {
                        if (lamp.meshIndex == i && lamp.isOn) {
                            emissiveColor = lamp.getEmissiveColor();
                            break;
                        }
                    }

                    glUniform3f(glGetUniformLocation(shaderProgram, "emissiveColor"),
                            emissiveColor[0], emissiveColor[1], emissiveColor[2]);

                    if (mesh.materialIndex >= 0 && mesh.materialIndex < materials.size()) {
                        Material mat = materials.get(mesh.materialIndex);

                        glUniform3f(glGetUniformLocation(shaderProgram, "diffuseColor"),
                                mat.diffuseColor[0], mat.diffuseColor[1], mat.diffuseColor[2]);

                        if (mat.hasTexture && mat.diffuseTextureId != 0) {
                            glUniform1i(glGetUniformLocation(shaderProgram, "hasTexture"), 1);
                            glActiveTexture(GL_TEXTURE0);
                            glBindTexture(GL_TEXTURE_2D, mat.diffuseTextureId);
                        } else {
                            glUniform1i(glGetUniformLocation(shaderProgram, "hasTexture"), 0);
                        }
                    } else {
                        glUniform3f(glGetUniformLocation(shaderProgram, "diffuseColor"), 0.5f, 0.5f, 0.5f);
                        glUniform1i(glGetUniformLocation(shaderProgram, "hasTexture"), 0);
                    }

                    glBindVertexArray(mesh.vaoId);
                    glDrawElements(GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_INT, 0);
                }
            } finally {
                sceneLock.unlock();
            }
        }

        glfwSwapBuffers(window);
    }

    public void applyTexturesToMaterials() {
        glTasks.add(() -> {
            TextureManager textureManager = TextureManager.getInstance();
            int appliedCount = 0;

            String texturesPath = "C:\\Users\\ZEI\\IdeaProjects\\SmartHomeConfigurator\\src\\main\\resources\\models\\textures";
            File texturesDir = new File(texturesPath);

            if (!texturesDir.exists() || !texturesDir.isDirectory()) {
                System.err.println("❌ Папка с текстурами не найдена: " + texturesPath);
                Platform.runLater(() -> {
                    if (infoLabel != null) {
                        infoLabel.setText("Ошибка: папка текстур не найдена!");
                    }
                });
                return;
            }

            // Загружаем все текстуры из папки
            Map<String, Integer> loadedTextures = new HashMap<>();
            File[] textureFiles = texturesDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".jpg") ||
                            name.toLowerCase().endsWith(".jpeg") ||
                            name.toLowerCase().endsWith(".png"));

            if (textureFiles != null) {
                for (File file : textureFiles) {
                    String fileName = file.getName();
                    // Убираем расширение файла для сопоставления
                    String textureName = fileName.substring(0, fileName.lastIndexOf('.'));
                    int textureId = textureManager.loadTexture(file.getAbsolutePath());
                    if (textureId != 0) {
                        loadedTextures.put(textureName.toLowerCase(), textureId);
                        System.out.println("✅ Загружена текстура: " + textureName + " -> ID: " + textureId);
                    }
                }
            }

            System.out.println("📊 Загружено текстур: " + loadedTextures.size());

            // Применяем текстуры к материалам
            sceneLock.lock();
            try {
                for (Material material : materials) {
                    if (material.name == null) continue;

                    // Получаем имя текстуры через TextureMapper
                    String textureName = TextureMapper.getTextureNameForMaterial(material.name);
                    if (textureName != null) {
                        String lowerTextureName = textureName.toLowerCase();

                        // Ищем загруженную текстуру
                        Integer textureId = loadedTextures.get(lowerTextureName);

                        // Если не нашли, пробуем искать без учета регистра и с разными вариациями
                        if (textureId == null) {
                            for (Map.Entry<String, Integer> entry : loadedTextures.entrySet()) {
                                if (entry.getKey().contains(lowerTextureName) ||
                                        lowerTextureName.contains(entry.getKey())) {
                                    textureId = entry.getValue();
                                    break;
                                }
                            }
                        }

                        if (textureId != null && textureId != 0) {
                            material.diffuseTextureId = textureId;
                            material.hasTexture = true;
                            appliedCount++;
                            System.out.println("🎨 Применена текстура '" + textureName + "' к материалу: " + material.name);
                        } else {
                            System.out.println("⚠️ Текстура не найдена для материала: " + material.name + " (искали: " + textureName + ")");
                        }
                    }
                }
            } finally {
                sceneLock.unlock();
            }

            final int finalAppliedCount = appliedCount;
            Platform.runLater(() -> {
                if (infoLabel != null) {
                    infoLabel.setText("Текстур применено: " + finalAppliedCount);
                }
            });

            System.out.println("✅ Применено текстур: " + appliedCount + " из " + materials.size() + " материалов");
        });
    }

    private void loadModelDialog() {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите 3D модель дома");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("OBJ файлы", "*.obj"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            File selectedFile = fileChooser.showOpenDialog(container.getScene().getWindow());
            if (selectedFile != null) {
                glTasks.add(() -> {
                    deleteHouseGL();
                    loadOBJFile(selectedFile);
                });

                Platform.runLater(() -> {
                    if (infoLabel != null) {
                        infoLabel.setText("Загрузка: " + selectedFile.getName());
                    }
                });
            }
        });
    }

    private void deleteHouse() {
        glTasks.add(this::deleteHouseGL);
        Platform.runLater(() -> {
            if (infoLabel != null) {
                infoLabel.setText("Модель удалена");
            }
        });
    }

    private void deleteHouseGL() {
        sceneLock.lock();
        try {
            for (Mesh mesh : meshes) {
                glDeleteVertexArrays(mesh.vaoId);
                glDeleteBuffers(mesh.vboId);
                if (mesh.vboNormalId != 0) glDeleteBuffers(mesh.vboNormalId);
                if (mesh.vboTexCoordId != 0) glDeleteBuffers(mesh.vboTexCoordId);
                glDeleteBuffers(mesh.eboId);
            }
            meshes.clear();
            materials.clear();
            lampObjects.clear();
            lampMap.clear();
        } finally {
            sceneLock.unlock();
        }
    }

    private void cleanup() {
        isRunning.set(false);
        deleteHouseGL();
        TextureManager.getInstance().cleanup();
        glDeleteProgram(shaderProgram);
        glfwDestroyWindow(window);
        glfwTerminate();
        if (framebuffer != null) {
            memFree(framebuffer);
        }
    }

    private void saveLastModelPath(String path) {
        this.lastModelPath = path;
        try {
            Files.write(Paths.get(CONFIG_FILE), path.getBytes());
        } catch (IOException e) {
        }
    }

    private void loadLastModelPath() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                lastModelPath = new String(Files.readAllBytes(configFile.toPath())).trim();
            }
        } catch (IOException e) {
        }
    }

    @Override
    public void clear() {
        isRunning.set(false);
        if (renderThread != null) {
            try {
                renderThread.join(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public String getName() {
        return "3D";
    }

    @Override
    public void handleClick(double x, double y) {
        // Используем только кнопки в панели управления
        System.out.println("Для управления лампами используйте панель управления (P)");
    }
}