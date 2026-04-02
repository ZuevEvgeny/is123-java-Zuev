package com.student.smarthomeconfigurator.generator;

import com.student.smarthomeconfigurator.model.building.FurnitureItem;
import com.student.smarthomeconfigurator.library.ItemLibrary;
import com.student.smarthomeconfigurator.library.LibraryItem;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class FurnitureModel3D {

    public static class FurnitureMeshData {
        public float[] vertices;
        public float[] normals;
        public float[] texCoords;
        public int[] indices;
        public float[] color;
        public Matrix4f transform;

        public FurnitureMeshData(float[] vertices, float[] normals, float[] texCoords, int[] indices,
                                 float r, float g, float b, Matrix4f transform) {
            this.vertices = vertices;
            this.normals = normals;
            this.texCoords = texCoords;
            this.indices = indices;
            this.color = new float[]{r, g, b};
            this.transform = transform;
        }
    }

    public static List<FurnitureMeshData> createFurnitureMesh(FurnitureItem item) {
        List<FurnitureMeshData> meshes = new ArrayList<>();

        ItemLibrary lib = ItemLibrary.getInstance();
        LibraryItem libItem = lib.getItem(item.getLibraryId());

        if (libItem == null) {
            return createDefaultCubeList(item);
        }

        float x = item.getX();
        float z = item.getZ();
        float rotation = item.getRotation();
        float scale = item.getScale();
        float y = item.getY();

        String placementSurface = (String) item.getParam("placementSurface");

        System.out.println("Создание меша для: " + item.getName() +
                " | Позиция: (" + x + ", " + y + ", " + z + ")" +
                " | Поверхность: " + placementSurface +
                " | Тип: " + libItem.getMountType());


        Matrix4f transform = new Matrix4f()
                .translate(x, y, z)
                .rotateY(rotation);

        ProceduralModelGenerator.ModelData modelData = null;

        if (libItem != null) {
            switch (libItem.getId()) {
                case "sofa_corner":
                    modelData = ProceduralModelGenerator.createCornerSofa(0, 0, 0, scale, 0);
                    break;
                case "bed_double":
                    modelData = ProceduralModelGenerator.createDoubleBed(0, 0, 0, scale, 0);
                    break;
                case "wardrobe":
                    modelData = ProceduralModelGenerator.createWardrobe(0, 0, 0, scale, 0);
                    break;
                case "shelf":
                    modelData = ProceduralModelGenerator.createShelf(0, 0, 0, scale, 0);
                    break;
                case "chair_modern":
                    modelData = ProceduralModelGenerator.createChair(0, 0, 0, scale, 0);
                    break;
                case "table_dining":
                    modelData = ProceduralModelGenerator.createTable(0, 0, 0, scale, 0);
                    break;
                case "coffee_table":
                    modelData = ProceduralModelGenerator.createCoffeeTable(0, 0, 0, scale, 0);
                    break;
                case "nightstand":
                    modelData = ProceduralModelGenerator.createNightstand(0, 0, 0, scale, 0);
                    break;
                case "tv":
                    modelData = ProceduralModelGenerator.createTV(0, 0, 0, scale, 0);
                    break;
                case "stove":
                    modelData = ProceduralModelGenerator.createStove(0, 0, 0, scale, 0);
                    break;
                case "fridge":
                    modelData = ProceduralModelGenerator.createFridge(0, 0, 0, scale, 0);
                    break;
                case "microwave":
                    modelData = ProceduralModelGenerator.createMicrowave(0, 0, 0, scale, 0);
                    break;
                case "dishwasher":
                    modelData = ProceduralModelGenerator.createDishwasher(0, 0, 0, scale, 0);
                    break;
                case "washing_machine":
                    modelData = ProceduralModelGenerator.createWashingMachine(0, 0, 0, scale, 0);
                    break;
                case "lamp_ceiling":
                    modelData = ProceduralModelGenerator.createChandelier(0, 0, 0, scale, 0);
                    break;
                case "lamp_pendant":
                    modelData = ProceduralModelGenerator.createPendantLamp(0, 0, 0, scale, 0);
                    break;
                case "lamp_desk":
                    modelData = ProceduralModelGenerator.createDeskLamp(0, 0, 0, scale, 0);
                    break;
                case "lamp_floor":
                    modelData = ProceduralModelGenerator.createFloorLamp(0, 0, 0, scale, 0);
                    break;
                case "lamp_spot":
                    modelData = ProceduralModelGenerator.createSpotlight(0, 0, 0, scale, 0);
                    break;
                case "sensor_motion":
                    modelData = ProceduralModelGenerator.createMotionSensor(0, 0, 0, scale, 0);
                    break;
                case "sensor_temp":
                    modelData = ProceduralModelGenerator.createTemperatureSensor(0, 0, 0, scale, 0);
                    break;
                case "sensor_smoke":
                    modelData = ProceduralModelGenerator.createSmokeSensor(0, 0, 0, scale, 0);
                    break;
                case "sensor_light":
                    modelData = ProceduralModelGenerator.createLightSensor(0, 0, 0, scale, 0);
                    break;
                default:
                    return createDefaultCubeList(item);
            }
        }

        if (modelData != null && !modelData.verticesList.isEmpty()) {
            for (int i = 0; i < modelData.verticesList.size(); i++) {

                Matrix4f componentTransform = new Matrix4f(transform);
                if (modelData.transforms != null && i < modelData.transforms.size() && modelData.transforms.get(i) != null) {
                    componentTransform.mul(modelData.transforms.get(i));
                }

                meshes.add(new FurnitureMeshData(
                        modelData.verticesList.get(i),
                        modelData.normalsList.get(i),
                        modelData.texCoordsList.get(i),
                        modelData.indicesList.get(i),
                        modelData.colors.get(i)[0],
                        modelData.colors.get(i)[1],
                        modelData.colors.get(i)[2],
                        componentTransform
                ));
            }
        } else {
            meshes = createDefaultCubeList(item);
        }

        return meshes;
    }

    private static List<FurnitureMeshData> createDefaultCubeList(FurnitureItem item) {
        List<FurnitureMeshData> meshes = new ArrayList<>();
        meshes.add(createDefaultCube(item));
        return meshes;
    }

    private static FurnitureMeshData createDefaultCube(FurnitureItem item) {
        float scale = item.getScale();
        float size = 0.4f * scale;
        float x = item.getX();
        float y = item.getY();
        float z = item.getZ();
        float rotation = item.getRotation();

        System.out.println("🔲 Создание куба для: " + item.getName() + " на позиции (" + x + ", " + y + ", " + z + ")");

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

        Matrix4f transform = new Matrix4f()
                .translate(x, y, z)
                .rotateY(rotation);

        float r, g, b;
        switch (item.getCategory()) {
            case "lamp":
                r = 1.0f; g = 0.8f; b = 0.0f;
                break;
            case "sensor":
                r = 0.0f; g = 0.8f; b = 1.0f;
                break;
            default:
                r = 0.6f; g = 0.4f; b = 0.2f;
        }

        return new FurnitureMeshData(vertices, normals, texCoords, indices, r, g, b, transform);
    }
}