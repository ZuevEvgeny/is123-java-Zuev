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
        float yOffset = libItem != null ? libItem.getDefaultYOffset() : 0f;
        float yPos = item.getY() == 0 ? yOffset : item.getY();

        ProceduralModelGenerator.ModelData modelData = null;

        if (libItem != null) {
            switch (libItem.getId()) {
                case "sofa_corner":
                    modelData = ProceduralModelGenerator.createCornerSofa(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "bed_double":
                    modelData = ProceduralModelGenerator.createDoubleBed(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "wardrobe":
                    modelData = ProceduralModelGenerator.createWardrobe(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "shelf":
                    modelData = ProceduralModelGenerator.createShelf(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "chair_modern":
                    modelData = ProceduralModelGenerator.createChair(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "table_dining":
                    modelData = ProceduralModelGenerator.createTable(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "lamp_ceiling":
                    modelData = ProceduralModelGenerator.createChandelier(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "lamp_pendant":
                    modelData = ProceduralModelGenerator.createPendantLamp(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "lamp_desk":
                    modelData = ProceduralModelGenerator.createDeskLamp(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "lamp_floor":
                    modelData = ProceduralModelGenerator.createFloorLamp(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "lamp_spot":
                    modelData = ProceduralModelGenerator.createSpotlight(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "sensor_motion":
                    modelData = ProceduralModelGenerator.createMotionSensor(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "sensor_temp":
                    modelData = ProceduralModelGenerator.createTemperatureSensor(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "sensor_smoke":
                    modelData = ProceduralModelGenerator.createSmokeSensor(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                case "sensor_light":
                    modelData = ProceduralModelGenerator.createLightSensor(item.getX(), yPos, item.getZ(),
                            item.getScale(), item.getRotation());
                    break;
                default:
                    meshes.add(createDefaultCube(item, yPos));
                    return meshes;
            }
        }

        if (modelData != null && !modelData.verticesList.isEmpty()) {
            for (int i = 0; i < modelData.verticesList.size(); i++) {
                meshes.add(new FurnitureMeshData(
                        modelData.verticesList.get(i),
                        modelData.normalsList.get(i),
                        modelData.texCoordsList.get(i),
                        modelData.indicesList.get(i),
                        modelData.colors.get(i)[0],
                        modelData.colors.get(i)[1],
                        modelData.colors.get(i)[2],
                        modelData.transforms.get(i)
                ));
            }
        } else {
            meshes.add(createDefaultCube(item, yPos));
        }

        return meshes;
    }

    private static FurnitureMeshData createDefaultCube(FurnitureItem item, float yPos) {
        float size = 0.4f * item.getScale();
        float x = item.getX();
        float y = yPos + size;
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

        Matrix4f transform = new Matrix4f()
                .translate(x, y, z)
                .rotateY(item.getRotation())
                .scale(item.getScale());

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