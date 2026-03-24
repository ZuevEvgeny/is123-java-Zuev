package com.student.smarthomeconfigurator.modules.renderer;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.building.*;
import com.student.smarthomeconfigurator.generator.BuildingMeshGenerator;
import com.student.smarthomeconfigurator.generator.FurnitureModel3D;
import com.student.smarthomeconfigurator.modules.simulator.DeviceSimulator;
import com.student.smarthomeconfigurator.utils.TextureManager;

import javafx.scene.layout.Pane;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.lang.Math;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class LWJGLRenderer implements MapRenderer {
    private Project currentProject;
    private Pane container;
    private DeviceSimulator currentSimulator;

    private Thread renderThread;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private BuildingProject currentBuildingProject;

    private BlockingQueue<Runnable> glTasks = new LinkedBlockingQueue<>();
    private final ReentrantLock sceneLock = new ReentrantLock();
    private List<Mesh> meshes = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();
    private List<LampObject> lampObjects = new ArrayList<>();

    private long window;
    private int width = 1024;
    private int height = 768;

    private int shaderProgram;

    private Vector3f cameraPos = new Vector3f(0.0f, 5.0f, 12.0f);
    private Vector3f cameraTarget = new Vector3f(0.0f, 1.5f, 0.0f);
    private Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

    private float cameraYaw = 0.0f;
    private float cameraPitch = -0.3f;
    private float targetYaw = 0.0f;
    private float targetPitch = -0.3f;

    private float moveSpeed = 5.0f;
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

    private long lastTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;

    private class Mesh {
        int vaoId;
        int vboId;
        int vboNormalId;
        int vboTexCoordId;
        int eboId;
        int indexCount;
        int materialIndex;
        Matrix4f transform;
    }

    private class Material {
        String name;
        float[] diffuseColor = {0.8f, 0.6f, 0.4f, 1.0f};
        int diffuseTextureId = 0;
        boolean hasTexture = false;
    }

    private class LampObject {
        int meshIndex;
        float[] position;
        boolean isOn;
        String name;
        float intensity;
        float targetIntensity;

        public LampObject(int meshIndex, float[] position, String name) {
            this.meshIndex = meshIndex;
            this.position = position;
            this.name = name;
            this.isOn = true;
            this.intensity = 1.0f;
            this.targetIntensity = 1.0f;
        }

        public void toggle() {
            isOn = !isOn;
            targetIntensity = isOn ? 1.0f : 0.0f;
            System.out.println("Лампа " + name + " " + (isOn ? "включена" : "выключена"));
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
            return new float[]{1.0f * intensity, 0.8f * intensity, 0.5f * intensity};
        }

        public float[] getEmissiveColor() {
            if (intensity <= 0.01f) return new float[]{0, 0, 0};
            float[] col = getLightColor();
            return new float[]{col[0] * 1.5f, col[1] * 1.5f, col[2] * 1.5f};
        }
    }

    @Override
    public void render(Project project, Pane container) {
        this.currentProject = project;
        this.container = container;

        javafx.application.Platform.runLater(() -> {
            container.getChildren().clear();
            createUI();
        });

        isRunning.set(true);
        renderThread = new Thread(this::runGLWindow);
        renderThread.setDaemon(true);
        renderThread.start();
    }

    private void createUI() {
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label("3D Окно открыто отдельно");
        infoLabel.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: #00ff00; -fx-padding: 10;");
        infoLabel.setLayoutX(10);
        infoLabel.setLayoutY(10);
        container.getChildren().add(infoLabel);

        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("✖ ЗАКРЫТЬ 3D");
        closeBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 10;");
        closeBtn.setLayoutX(10);
        closeBtn.setLayoutY(50);
        closeBtn.setOnAction(e -> stopGL());
        container.getChildren().add(closeBtn);

        javafx.scene.control.Label controlsLabel = new javafx.scene.control.Label(
                "УПРАВЛЕНИЕ:\n" +
                        "WASD - ходить\n" +
                        "Q/E - вверх/вниз\n" +
                        "Мышь - осмотр\n" +
                        "O - орбита/ходьба\n" +
                        "R - сброс камеры"
        );
        controlsLabel.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: #ffaa00; -fx-padding: 8;");
        controlsLabel.setLayoutX(10);
        controlsLabel.setLayoutY(90);
        container.getChildren().add(controlsLabel);
    }

    public void loadBuildingProject(BuildingProject project) {
        this.currentBuildingProject = project;
        if (window != 0) {
            glfwPostEmptyEvent();
        }
    }

    public void setSimulator(DeviceSimulator simulator) {
        this.currentSimulator = simulator;
    }

    public void stopGL() {
        isRunning.set(false);
        if (renderThread != null) {
            try {
                renderThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (window != 0) {
            glfwDestroyWindow(window);
            window = 0;
        }
        glfwTerminate();
    }

    private void runGLWindow() {
        try {
            initGLFW();
            initOpenGL();
            initShaders();

            if (currentBuildingProject != null) {
                loadScene();
            }

            while (isRunning.get() && !glfwWindowShouldClose(window)) {
                double deltaTime = 0.016;
                updateMovement(deltaTime);
                render();

                frames++;
                long currentTime = System.nanoTime();
                if (currentTime - lastTime >= 1_000_000_000) {
                    fps = frames;
                    frames = 0;
                    lastTime = currentTime;
                    System.out.println("FPS: " + fps + " | Мешей: " + meshes.size() + " | Ламп: " + lampObjects.size());
                }

                glfwSwapBuffers(window);
                glfwPollEvents();
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
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, "3D Smart Home Configurator", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Не могу создать окно GLFW");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W: moveForward = true; break;
                    case GLFW_KEY_S: moveBackward = true; break;
                    case GLFW_KEY_A: moveLeft = true; break;
                    case GLFW_KEY_D: moveRight = true; break;
                    case GLFW_KEY_Q: moveUp = true; break;
                    case GLFW_KEY_E: moveDown = true; break;
                    case GLFW_KEY_R: resetCamera(); break;
                    case GLFW_KEY_O: orbitMode = !orbitMode; break;
                }
            } else if (action == GLFW_RELEASE) {
                switch (key) {
                    case GLFW_KEY_W: moveForward = false; break;
                    case GLFW_KEY_S: moveBackward = false; break;
                    case GLFW_KEY_A: moveLeft = false; break;
                    case GLFW_KEY_D: moveRight = false; break;
                    case GLFW_KEY_Q: moveUp = false; break;
                    case GLFW_KEY_E: moveDown = false; break;
                }
            }
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    mousePressed = true;
                    double[] x = new double[1];
                    double[] y = new double[1];
                    glfwGetCursorPos(window, x, y);
                    lastMouseX = x[0];
                    lastMouseY = y[0];
                } else {
                    mousePressed = false;
                }
            }
        });

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (mousePressed) {
                double dx = xpos - lastMouseX;
                double dy = ypos - lastMouseY;
                targetYaw -= dx * mouseSensitivity;
                targetPitch += dy * mouseSensitivity;
                targetPitch = (float) Math.max(-1.4, Math.min(1.4, targetPitch));
                lastMouseX = xpos;
                lastMouseY = ypos;
            }
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            if (orbitMode) {
                Vector3f direction = new Vector3f(cameraTarget).sub(cameraPos).normalize();
                float delta = (float)yoffset * zoomSpeed * 0.1f;
                cameraPos.add(direction.mul(delta));
            } else {
                Vector3f forward = new Vector3f(cameraTarget).sub(cameraPos).normalize();
                float delta = (float)yoffset * zoomSpeed * 0.1f;
                cameraPos.add(forward.mul(delta));
                cameraTarget.add(forward.mul(delta));
            }
        });
    }

    private void initOpenGL() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(0.2f, 0.2f, 0.3f, 1.0f);
    }

    private void initShaders() {
        String vertexShaderSource = "#version 330 core\n" +
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

        String fragmentShaderSource = "#version 330 core\n" +
                "out vec4 FragColor;\n" +
                "in vec3 FragPos;\n" +
                "in vec3 Normal;\n" +
                "in vec2 TexCoord;\n" +
                "uniform sampler2D texture1;\n" +
                "uniform bool hasTexture;\n" +
                "uniform vec3 diffuseColor;\n" +
                "uniform vec3 emissiveColor;\n" +
                "uniform vec3 lightDir;\n" +
                "uniform vec3 lightColor;\n" +
                "uniform vec3 viewPos;\n" +
                "void main() {\n" +
                "    vec3 norm = normalize(Normal);\n" +
                "    vec3 light = normalize(lightDir);\n" +
                "    float diff = max(dot(norm, light), 0.3);\n" +
                "    vec3 ambient = vec3(0.3, 0.3, 0.3);\n" +
                "    vec3 result = (ambient + diff) * diffuseColor;\n" +
                "    result += emissiveColor;\n" +
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

    private void loadScene() {
        deleteAllMeshes();
        lampObjects.clear();

        List<BuildingMeshGenerator.MeshData> generatedMeshes = BuildingMeshGenerator.generateBuilding(currentBuildingProject);

        if (generatedMeshes != null) {
            for (BuildingMeshGenerator.MeshData meshData : generatedMeshes) {
                if (meshData != null) {
                    createMeshFromData(meshData);
                }
            }
        }

        // Загружаем мебель
        for (FurnitureItem item : currentBuildingProject.getFurniture()) {
            if (item.getCategory().equals("lamp")) {
                float[] pos = {item.getX(), item.getY() + 0.5f, item.getZ()};
                LampObject lamp = new LampObject(meshes.size(), pos, item.getName());
                lampObjects.add(lamp);
            }

            try {
                List<FurnitureModel3D.FurnitureMeshData> furnitureDataList = FurnitureModel3D.createFurnitureMesh(item);
                if (furnitureDataList != null) {
                    for (FurnitureModel3D.FurnitureMeshData furnitureData : furnitureDataList) {
                        if (furnitureData != null && furnitureData.vertices != null) {
                            createFurnitureMesh(furnitureData);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Ошибка загрузки мебели: " + item.getName() + " - " + e.getMessage());
                createDefaultCube(item);
            }
        }

        System.out.println("✅ 3D сцена загружена: стен=" + currentBuildingProject.getWalls().size() +
                ", предметов=" + currentBuildingProject.getFurniture().size() +
                ", ламп=" + lampObjects.size());
    }

    private void createMeshFromData(BuildingMeshGenerator.MeshData data) {
        if (data == null || data.vertices == null || data.vertices.length == 0) {
            return;
        }

        Mesh mesh = new Mesh();
        mesh.indexCount = data.indices.length;
        mesh.materialIndex = materials.size();
        mesh.transform = new Matrix4f().identity();

        mesh.vaoId = glGenVertexArrays();
        glBindVertexArray(mesh.vaoId);

        mesh.vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mesh.vboId);
        FloatBuffer vertexBuffer = memAllocFloat(data.vertices.length);
        vertexBuffer.put(data.vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        memFree(vertexBuffer);

        if (data.normals != null && data.normals.length > 0) {
            mesh.vboNormalId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, mesh.vboNormalId);
            FloatBuffer normalBuffer = memAllocFloat(data.normals.length);
            normalBuffer.put(data.normals).flip();
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);
            memFree(normalBuffer);
        }

        if (data.texCoords != null && data.texCoords.length > 0) {
            mesh.vboTexCoordId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, mesh.vboTexCoordId);
            FloatBuffer texBuffer = memAllocFloat(data.texCoords.length);
            texBuffer.put(data.texCoords).flip();
            glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
            memFree(texBuffer);
        }

        mesh.eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.eboId);
        IntBuffer indexBuffer = memAllocInt(data.indices.length);
        indexBuffer.put(data.indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        sceneLock.lock();
        try {
            meshes.add(mesh);

            Material mat = new Material();
            mat.name = data.textureName != null ? data.textureName : "default";
            mat.diffuseColor = data.color != null ? data.color : new float[]{0.8f, 0.6f, 0.4f, 1.0f};
            mat.hasTexture = false;

            TextureManager texManager = TextureManager.getInstance();
            if (data.textureName != null) {
                switch (data.textureName) {
                    case "brick":
                        mat.diffuseTextureId = texManager.generateBrickTexture();
                        mat.hasTexture = true;
                        break;
                    case "floor_wood":
                    case "wood":
                        mat.diffuseTextureId = texManager.generateWoodTexture();
                        mat.hasTexture = true;
                        break;
                    case "door":
                        mat.diffuseTextureId = texManager.generateDoorTexture();
                        mat.hasTexture = true;
                        break;
                }
            }

            materials.add(mat);
        } finally {
            sceneLock.unlock();
        }
    }

    private void createFurnitureMesh(FurnitureModel3D.FurnitureMeshData data) {
        if (data == null || data.vertices == null || data.vertices.length == 0) {
            return;
        }

        Mesh mesh = new Mesh();
        mesh.indexCount = data.indices.length;
        mesh.materialIndex = materials.size();
        mesh.transform = data.transform;

        mesh.vaoId = glGenVertexArrays();
        glBindVertexArray(mesh.vaoId);

        mesh.vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mesh.vboId);
        FloatBuffer vertexBuffer = memAllocFloat(data.vertices.length);
        vertexBuffer.put(data.vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        memFree(vertexBuffer);

        if (data.normals != null && data.normals.length > 0) {
            mesh.vboNormalId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, mesh.vboNormalId);
            FloatBuffer normalBuffer = memAllocFloat(data.normals.length);
            normalBuffer.put(data.normals).flip();
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);
            memFree(normalBuffer);
        }

        if (data.texCoords != null && data.texCoords.length > 0) {
            mesh.vboTexCoordId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, mesh.vboTexCoordId);
            FloatBuffer texBuffer = memAllocFloat(data.texCoords.length);
            texBuffer.put(data.texCoords).flip();
            glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
            memFree(texBuffer);
        }

        mesh.eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.eboId);
        IntBuffer indexBuffer = memAllocInt(data.indices.length);
        indexBuffer.put(data.indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        sceneLock.lock();
        try {
            meshes.add(mesh);

            Material mat = new Material();
            mat.name = "furniture";
            mat.diffuseColor = data.color != null ? data.color : new float[]{0.6f, 0.4f, 0.2f, 1.0f};
            mat.hasTexture = false;
            materials.add(mat);
        } finally {
            sceneLock.unlock();
        }
    }

    private void createDefaultCube(FurnitureItem item) {
        float size = 0.4f * item.getScale();
        float x = item.getX();
        float y = item.getY() + size;
        float z = item.getZ();

        float[] vertices = {
                -size, -size, -size,  size, -size, -size,  size,  size, -size, -size,  size, -size,
                -size, -size,  size,  size, -size,  size,  size,  size,  size, -size,  size,  size
        };

        float[] normals = new float[24];
        for (int i = 0; i < 4; i++) normals[i*3+2] = -1;
        for (int i = 4; i < 8; i++) normals[i*3+2] = 1;
        for (int i = 0; i < 8; i+=4) normals[i*3+1] = -1;
        for (int i = 2; i < 8; i+=4) normals[i*3+1] = 1;
        normals[0*3] = -1; normals[3*3] = -1; normals[4*3] = -1; normals[7*3] = -1;
        normals[1*3] = 1; normals[2*3] = 1; normals[5*3] = 1; normals[6*3] = 1;

        float[] texCoords = new float[16];
        for (int i = 0; i < 4; i++) {
            texCoords[i*2] = 0;
            texCoords[i*2+1] = 0;
        }
        for (int i = 4; i < 8; i++) {
            texCoords[i*2] = 1;
            texCoords[i*2+1] = 1;
        }

        int[] indices = {
                0,1,2, 0,2,3,
                4,6,5, 4,7,6,
                0,4,1, 1,4,5,
                3,2,7, 2,6,7,
                0,3,4, 3,7,4,
                1,5,2, 2,5,6
        };

        Mesh mesh = new Mesh();
        mesh.indexCount = indices.length;
        mesh.materialIndex = materials.size();
        mesh.transform = new Matrix4f().translate(x, y, z).rotateY(item.getRotation()).scale(item.getScale());

        mesh.vaoId = glGenVertexArrays();
        glBindVertexArray(mesh.vaoId);

        mesh.vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mesh.vboId);
        FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        memFree(vertexBuffer);

        mesh.eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.eboId);
        IntBuffer indexBuffer = memAllocInt(indices.length);
        indexBuffer.put(indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        memFree(indexBuffer);

        sceneLock.lock();
        try {
            meshes.add(mesh);

            Material mat = new Material();
            mat.name = "default_cube";
            mat.diffuseColor = new float[]{0.8f, 0.6f, 0.4f, 1.0f};
            mat.hasTexture = false;
            materials.add(mat);
        } finally {
            sceneLock.unlock();
        }
    }

    private void deleteAllMeshes() {
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
        } finally {
            sceneLock.unlock();
        }
    }

    private void updateMovement(double deltaTime) {
        cameraYaw += (targetYaw - cameraYaw) * rotationLerpSpeed * (float)deltaTime;
        cameraPitch += (targetPitch - cameraPitch) * rotationLerpSpeed * (float)deltaTime;

        float cosPitch = (float)Math.cos(cameraPitch);
        float sinPitch = (float)Math.sin(cameraPitch);
        float cosYaw = (float)Math.cos(cameraYaw);
        float sinYaw = (float)Math.sin(cameraYaw);

        Vector3f forward = new Vector3f(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
        Vector3f right = new Vector3f(cosYaw, 0, -sinYaw);
        Vector3f up = new Vector3f(0, 1, 0);

        float speed = moveSpeed * (float)deltaTime;

        if (orbitMode) {
            float distance = (float) Math.hypot(cameraPos.x, cameraPos.z);
            float newX = (float) (Math.sin(cameraYaw) * distance);
            float newZ = (float) (Math.cos(cameraYaw) * distance);
            float newY = (float) (Math.sin(cameraPitch) * distance + 2.0f);

            cameraPos.set(newX, newY, newZ);
            cameraTarget.set(0.0f, 1.5f, 0.0f);
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

    private void resetCamera() {
        cameraPos.set(0.0f, 5.0f, 12.0f);
        cameraTarget.set(0.0f, 1.5f, 0.0f);
        cameraYaw = 0.0f;
        cameraPitch = -0.3f;
        targetYaw = 0.0f;
        targetPitch = -0.3f;
        orbitMode = true;
    }

    private void render() {
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetFramebufferSize(window, w, h);
        glViewport(0, 0, w[0], h[0]);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);

        Matrix4f projection = new Matrix4f().perspective((float)Math.toRadians(60.0f), (float)w[0]/h[0], 0.1f, 100.0f);
        Matrix4f view = new Matrix4f().lookAt(cameraPos, cameraTarget, cameraUp);

        double deltaTime = 0.016;
        for (LampObject lamp : lampObjects) {
            lamp.updateAnimation(deltaTime);
        }

        int lightDirLoc = glGetUniformLocation(shaderProgram, "lightDir");
        if (lightDirLoc >= 0) {
            glUniform3f(lightDirLoc, 0.5f, 1.0f, 0.3f);
        }

        int lightColorLoc = glGetUniformLocation(shaderProgram, "lightColor");
        if (lightColorLoc >= 0) {
            glUniform3f(lightColorLoc, 1.0f, 1.0f, 1.0f);
        }

        int viewPosLoc = glGetUniformLocation(shaderProgram, "viewPos");
        if (viewPosLoc >= 0) {
            glUniform3f(viewPosLoc, cameraPos.x, cameraPos.y, cameraPos.z);
        }

        try (MemoryStack stack = stackPush()) {
            FloatBuffer matrixBuffer = stack.mallocFloat(16);

            projection.get(matrixBuffer);
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "projection"), false, matrixBuffer);

            view.get(matrixBuffer);
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "view"), false, matrixBuffer);

            sceneLock.lock();
            try {
                for (int i = 0; i < meshes.size(); i++) {
                    Mesh mesh = meshes.get(i);

                    if (mesh.transform != null) {
                        mesh.transform.get(matrixBuffer);
                    } else {
                        new Matrix4f().identity().get(matrixBuffer);
                    }
                    glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, matrixBuffer);

                    float[] emissiveColor = new float[]{0, 0, 0};
                    for (LampObject lamp : lampObjects) {
                        if (lamp.meshIndex == i) {
                            emissiveColor = lamp.getEmissiveColor();
                            break;
                        }
                    }

                    int emissiveLoc = glGetUniformLocation(shaderProgram, "emissiveColor");
                    if (emissiveLoc >= 0) {
                        glUniform3f(emissiveLoc, emissiveColor[0], emissiveColor[1], emissiveColor[2]);
                    }

                    if (mesh.materialIndex >= 0 && mesh.materialIndex < materials.size()) {
                        Material mat = materials.get(mesh.materialIndex);
                        glUniform3f(glGetUniformLocation(shaderProgram, "diffuseColor"),
                                mat.diffuseColor[0], mat.diffuseColor[1], mat.diffuseColor[2]);
                        glUniform1i(glGetUniformLocation(shaderProgram, "hasTexture"), mat.hasTexture ? 1 : 0);

                        if (mat.hasTexture && mat.diffuseTextureId != 0) {
                            glActiveTexture(GL_TEXTURE0);
                            glBindTexture(GL_TEXTURE_2D, mat.diffuseTextureId);
                        }
                    } else {
                        glUniform3f(glGetUniformLocation(shaderProgram, "diffuseColor"), 0.7f, 0.7f, 0.7f);
                        glUniform1i(glGetUniformLocation(shaderProgram, "hasTexture"), 0);
                    }

                    glBindVertexArray(mesh.vaoId);
                    glDrawElements(GL_TRIANGLES, mesh.indexCount, GL_UNSIGNED_INT, 0);
                }
            } finally {
                sceneLock.unlock();
            }
        }
    }

    private void cleanup() {
        isRunning.set(false);
        deleteAllMeshes();
        if (shaderProgram != 0) glDeleteProgram(shaderProgram);
        if (window != 0) glfwDestroyWindow(window);
        glfwTerminate();
    }

    @Override
    public void clear() {
        stopGL();
    }

    @Override
    public String getName() {
        return "3D";
    }

    @Override
    public void handleClick(double x, double y) {}
}