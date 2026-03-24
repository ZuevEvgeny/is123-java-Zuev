package com.student.smarthomeconfigurator.generator;

import com.student.smarthomeconfigurator.model.building.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingMeshGenerator {

    public static class MeshData {
        public float[] vertices;
        public float[] normals;
        public float[] texCoords;
        public int[] indices;
        public float[] color;
        public String textureName;

        public MeshData(float[] vertices, float[] normals, float[] texCoords, int[] indices, float r, float g, float b) {
            this(vertices, normals, texCoords, indices, r, g, b, null);
        }

        public MeshData(float[] vertices, float[] normals, float[] texCoords, int[] indices, float r, float g, float b, String textureName) {
            this.vertices = vertices;
            this.normals = normals;
            this.texCoords = texCoords;
            this.indices = indices;
            this.color = new float[]{r, g, b};
            this.textureName = textureName;
        }
    }

    public static List<MeshData> generateBuilding(BuildingProject project) {
        List<MeshData> meshes = new ArrayList<>();

        System.out.println("🏗️ Генерация 3D сцены...");
        System.out.println("   Стен: " + project.getWalls().size());

        // Пол
        MeshData floor = createFloorMesh(project);
        if (floor != null) {
            meshes.add(floor);
        }

        // Стены
        for (WallSegment wall : project.getWalls()) {
            List<MeshData> wallMeshes = createWallWithOpening(wall);
            if (wallMeshes != null) {
                // ✅ Фильтруем null
                wallMeshes.removeIf(mesh -> mesh == null);
                if (!wallMeshes.isEmpty()) {
                    meshes.addAll(wallMeshes);
                }
            }
        }

        // ✅ Удаляем null из общего списка
        meshes.removeIf(mesh -> mesh == null);

        System.out.println("✅ Сгенерировано мешей: " + meshes.size());
        return meshes;
    }

    private static MeshData createFloorMesh(BuildingProject project) {
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (WallSegment wall : project.getWalls()) {
            minX = Math.min(minX, Math.min(wall.getX1(), wall.getX2()));
            maxX = Math.max(maxX, Math.max(wall.getX1(), wall.getX2()));
            minZ = Math.min(minZ, Math.min(wall.getZ1(), wall.getZ2()));
            maxZ = Math.max(maxZ, Math.max(wall.getZ1(), wall.getZ2()));
        }

        if (project.getWalls().isEmpty()) {
            minX = -5; maxX = 5; minZ = -5; maxZ = 5;
        }

        minX -= 0.2f;
        maxX += 0.2f;
        minZ -= 0.2f;
        maxZ += 0.2f;

        float width = maxX - minX;
        float depth = maxZ - minZ;

        float[] vertices = {
                minX, -0.05f, minZ,
                maxX, -0.05f, minZ,
                maxX, -0.05f, maxZ,
                minX, -0.05f, maxZ
        };

        float[] normals = {0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0};

        float[] texCoords = {
                0, 0,
                width / 2, 0,
                width / 2, depth / 2,
                0, depth / 2
        };

        int[] indices = {0, 2, 1, 0, 3, 2};

        return new MeshData(vertices, normals, texCoords, indices, 0.3f, 0.3f, 0.3f, "floor_wood");
    }

    private static List<MeshData> createWallWithOpening(WallSegment wall) {
        List<MeshData> meshes = new ArrayList<>();

        float x1 = wall.getX1();
        float z1 = wall.getZ1();
        float x2 = wall.getX2();
        float z2 = wall.getZ2();
        float height = wall.getHeight();
        float thickness = wall.getThickness();

        float length = (float) Math.hypot(x2 - x1, z2 - z1);
        if (length < 0.01f) return null;

        float centerX = (x1 + x2) / 2;
        float centerZ = (z1 + z2) / 2;
        float angle = (float) Math.atan2(z2 - z1, x2 - x1);

        if (wall.getType() == WallSegment.WallType.INNER) {
            meshes.add(createSolidWall(wall, length, centerX, centerZ, angle, height, thickness));
            return meshes;
        }

        float openingWidth = wall.getOpeningWidth();
        float openingHeight = wall.getOpeningHeight();

        float openingY;
        if (wall.getType() == WallSegment.WallType.DOOR) {
            openingY = 0;
        } else {
            openingY = (height - openingHeight) / 2;
        }

        // Левая часть стены
        float leftWidth = (length - openingWidth) / 2;
        if (leftWidth > 0.01f) {
            meshes.add(createWallPart(wall, length, centerX, centerZ, angle, height, thickness,
                    -length/2, leftWidth, 0, height));
        }

        // Правая часть стены
        float rightStart = length/2 - (length - openingWidth)/2;
        if (rightStart > 0.01f) {
            meshes.add(createWallPart(wall, length, centerX, centerZ, angle, height, thickness,
                    rightStart, (length - openingWidth)/2, 0, height));
        }

        // Для двери: добавляем саму дверь
        if (wall.getType() == WallSegment.WallType.DOOR) {
            meshes.add(createDoorModel(wall, length, centerX, centerZ, angle, openingWidth, openingHeight, openingY, thickness));
        }

        // Для окна: нижняя часть под проемом
        if (wall.getType() == WallSegment.WallType.WINDOW && openingY > 0.01f) {
            meshes.add(createWallPart(wall, length, centerX, centerZ, angle, height, thickness,
                    -openingWidth/2, openingWidth, 0, openingY));
        }

        // Верхняя часть над проемом
        float topY = openingY + openingHeight;
        float topHeight = height - topY;
        if (topHeight > 0.01f) {
            meshes.add(createWallPart(wall, length, centerX, centerZ, angle, height, thickness,
                    -openingWidth/2, openingWidth, topY, topHeight));
        }

        // Рама и стекло для окна
        if (wall.getType() == WallSegment.WallType.WINDOW && openingWidth > 0 && openingHeight > 0) {
            meshes.add(createWindowFrame(wall, length, centerX, centerZ, angle,
                    openingWidth, openingHeight, openingY, thickness));
            meshes.add(createWindowGlass(wall, length, centerX, centerZ, angle,
                    openingWidth, openingHeight, openingY, thickness));
        }

        return meshes;
    }

    private static MeshData createSolidWall(WallSegment wall, float length, float centerX, float centerZ,
                                            float angle, float height, float thickness) {
        float halfLength = length / 2;
        float halfHeight = height / 2;
        float halfThick = thickness / 2;

        float[] localVerts = {
                -halfLength, -halfHeight, -halfThick,
                halfLength, -halfHeight, -halfThick,
                halfLength,  halfHeight, -halfThick,
                -halfLength,  halfHeight, -halfThick,
                -halfLength, -halfHeight,  halfThick,
                halfLength, -halfHeight,  halfThick,
                halfLength,  halfHeight,  halfThick,
                -halfLength,  halfHeight,  halfThick
        };

        float[] vertices = new float[24];
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        for (int i = 0; i < 8; i++) {
            float lx = localVerts[i*3];
            float ly = localVerts[i*3+1];
            float lz = localVerts[i*3+2];

            float rx = lx * cos + lz * sin;
            float rz = -lx * sin + lz * cos;

            vertices[i*3] = rx + centerX;
            vertices[i*3+1] = ly + halfHeight;
            vertices[i*3+2] = rz + centerZ;
        }

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
            texCoords[i*2] = length / 2;
            texCoords[i*2+1] = height;
        }

        int[] indices = {
                0,1,2, 0,2,3,
                4,6,5, 4,7,6,
                0,4,1, 1,4,5,
                3,2,7, 2,6,7,
                0,3,4, 3,7,4,
                1,5,2, 2,5,6
        };

        return new MeshData(vertices, normals, texCoords, indices, 0.8f, 0.7f, 0.6f, "brick");
    }

    private static MeshData createWallPart(WallSegment wall, float totalLength, float centerX, float centerZ,
                                           float angle, float wallHeight, float thickness,
                                           float startX, float width, float startY, float height) {
        if (width <= 0.01f || height <= 0.01f) return null;

        float halfWidth = width / 2;
        float halfHeight = height / 2;
        float halfThick = thickness / 2;

        float localCenterX = startX + halfWidth;
        float localCenterY = startY + halfHeight;

        float[] localVerts = {
                -halfWidth, -halfHeight, -halfThick,
                halfWidth, -halfHeight, -halfThick,
                halfWidth,  halfHeight, -halfThick,
                -halfWidth,  halfHeight, -halfThick,
                -halfWidth, -halfHeight,  halfThick,
                halfWidth, -halfHeight,  halfThick,
                halfWidth,  halfHeight,  halfThick,
                -halfWidth,  halfHeight,  halfThick
        };

        float[] vertices = new float[24];
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        for (int i = 0; i < 8; i++) {
            float lx = localVerts[i*3];
            float ly = localVerts[i*3+1];
            float lz = localVerts[i*3+2];

            float rx = (lx + localCenterX) * cos + lz * sin;
            float rz = -(lx + localCenterX) * sin + lz * cos;

            vertices[i*3] = rx + centerX;
            vertices[i*3+1] = ly + localCenterY;
            vertices[i*3+2] = rz + centerZ;
        }

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
            texCoords[i*2] = width;
            texCoords[i*2+1] = height;
        }

        int[] indices = {
                0,1,2, 0,2,3,
                4,6,5, 4,7,6,
                0,4,1, 1,4,5,
                3,2,7, 2,6,7,
                0,3,4, 3,7,4,
                1,5,2, 2,5,6
        };

        String textureName;
        if (wall.getType() == WallSegment.WallType.DOOR) {
            textureName = "door";
        } else if (wall.getType() == WallSegment.WallType.WINDOW) {
            textureName = "wall_light";
        } else {
            textureName = "brick";
        }

        return new MeshData(vertices, normals, texCoords, indices, 0.8f, 0.7f, 0.6f, textureName);
    }

    private static MeshData createDoorModel(WallSegment wall, float totalLength, float centerX, float centerZ,
                                            float angle, float width, float height, float yPos, float thickness) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        float halfThick = thickness / 2 + 0.01f;
        float centerY = yPos + height / 2;

        float[] localVerts = {
                -halfWidth, -halfHeight, -halfThick,
                halfWidth, -halfHeight, -halfThick,
                halfWidth,  halfHeight, -halfThick,
                -halfWidth,  halfHeight, -halfThick,
                -halfWidth, -halfHeight,  halfThick,
                halfWidth, -halfHeight,  halfThick,
                halfWidth,  halfHeight,  halfThick,
                -halfWidth,  halfHeight,  halfThick
        };

        float[] vertices = new float[24];
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        for (int i = 0; i < 8; i++) {
            float lx = localVerts[i*3];
            float ly = localVerts[i*3+1];
            float lz = localVerts[i*3+2];

            float rx = lx * cos + lz * sin;
            float rz = -lx * sin + lz * cos;

            vertices[i*3] = rx + centerX;
            vertices[i*3+1] = ly + centerY;
            vertices[i*3+2] = rz + centerZ;
        }

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

        return new MeshData(vertices, normals, texCoords, indices, 0.6f, 0.4f, 0.2f, "door");
    }

    private static MeshData createWindowFrame(WallSegment wall, float totalLength, float centerX, float centerZ,
                                              float angle, float width, float height, float yPos, float thickness) {
        float halfThick = thickness / 2 + 0.02f;
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        float centerY = yPos + height / 2;
        float frameWidth = 0.05f;

        List<Float> verts = new ArrayList<>();
        List<Float> norms = new ArrayList<>();
        List<Float> tex = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();
        int vertexOffset = 0;

        // Верхняя планка
        addFramePiece(verts, norms, tex, idx, vertexOffset,
                -halfWidth + frameWidth, centerY + halfHeight - frameWidth,
                halfWidth - frameWidth, centerY + halfHeight,
                halfThick, angle, centerX, centerZ);
        vertexOffset += 8;

        // Нижняя планка
        addFramePiece(verts, norms, tex, idx, vertexOffset,
                -halfWidth + frameWidth, centerY - halfHeight,
                halfWidth - frameWidth, centerY - halfHeight + frameWidth,
                halfThick, angle, centerX, centerZ);
        vertexOffset += 8;

        // Левая планка
        addFramePiece(verts, norms, tex, idx, vertexOffset,
                -halfWidth, centerY - halfHeight + frameWidth,
                -halfWidth + frameWidth, centerY + halfHeight - frameWidth,
                halfThick, angle, centerX, centerZ);
        vertexOffset += 8;

        // Правая планка
        addFramePiece(verts, norms, tex, idx, vertexOffset,
                halfWidth - frameWidth, centerY - halfHeight + frameWidth,
                halfWidth, centerY + halfHeight - frameWidth,
                halfThick, angle, centerX, centerZ);
        vertexOffset += 8;

        if (verts.isEmpty()) return null;

        float[] vertices = new float[verts.size()];
        float[] normals = new float[norms.size()];
        float[] texCoords = new float[tex.size()];
        int[] indices = new int[idx.size()];

        for (int i = 0; i < verts.size(); i++) vertices[i] = verts.get(i);
        for (int i = 0; i < norms.size(); i++) normals[i] = norms.get(i);
        for (int i = 0; i < tex.size(); i++) texCoords[i] = tex.get(i);
        for (int i = 0; i < idx.size(); i++) indices[i] = idx.get(i);

        return new MeshData(vertices, normals, texCoords, indices, 0.9f, 0.85f, 0.7f, "window_frame");
    }

    private static MeshData createWindowGlass(WallSegment wall, float totalLength, float centerX, float centerZ,
                                              float angle, float width, float height, float yPos, float thickness) {
        float halfWidth = width / 2 - 0.05f;
        float halfHeight = height / 2 - 0.05f;
        float halfThick = 0.02f;
        float centerY = yPos + height / 2;

        float[] localVerts = {
                -halfWidth, -halfHeight, -halfThick,
                halfWidth, -halfHeight, -halfThick,
                halfWidth,  halfHeight, -halfThick,
                -halfWidth,  halfHeight, -halfThick,
                -halfWidth, -halfHeight,  halfThick,
                halfWidth, -halfHeight,  halfThick,
                halfWidth,  halfHeight,  halfThick,
                -halfWidth,  halfHeight,  halfThick
        };

        float[] vertices = new float[24];
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        for (int i = 0; i < 8; i++) {
            float lx = localVerts[i*3];
            float ly = localVerts[i*3+1];
            float lz = localVerts[i*3+2];

            float rx = lx * cos + lz * sin;
            float rz = -lx * sin + lz * cos;

            vertices[i*3] = rx + centerX;
            vertices[i*3+1] = ly + centerY;
            vertices[i*3+2] = rz + centerZ;
        }

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

        return new MeshData(vertices, normals, texCoords, indices, 0.7f, 0.8f, 0.9f, "window_glass");
    }

    private static void addFramePiece(List<Float> verts, List<Float> norms, List<Float> tex, List<Integer> idx,
                                      int offset, float x1, float y1, float x2, float y2, float z,
                                      float angle, float centerX, float centerZ) {
        float halfWidth = (x2 - x1) / 2;
        float halfHeight = (y2 - y1) / 2;
        float centerX_local = (x1 + x2) / 2;
        float centerY_local = (y1 + y2) / 2;
        float halfThick = z;

        float[] localVerts = {
                -halfWidth, -halfHeight, -halfThick,
                halfWidth, -halfHeight, -halfThick,
                halfWidth,  halfHeight, -halfThick,
                -halfWidth,  halfHeight, -halfThick,
                -halfWidth, -halfHeight,  halfThick,
                halfWidth, -halfHeight,  halfThick,
                halfWidth,  halfHeight,  halfThick,
                -halfWidth,  halfHeight,  halfThick
        };

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        for (int i = 0; i < 8; i++) {
            float lx = localVerts[i*3];
            float ly = localVerts[i*3+1];
            float lz = localVerts[i*3+2];

            float rx = (lx + centerX_local) * cos + lz * sin;
            float rz = -(lx + centerX_local) * sin + lz * cos;

            verts.add(rx + centerX);
            verts.add(ly + centerY_local);
            verts.add(rz + centerZ);

            if (i < 4) {
                norms.add(0f); norms.add(0f); norms.add(-1f);
            } else {
                norms.add(0f); norms.add(0f); norms.add(1f);
            }

            tex.add((i % 4 < 2) ? 0f : 1f);
            tex.add((i % 4 == 0 || i % 4 == 3) ? 0f : 1f);
        }

        int[] indices = {
                offset, offset+1, offset+2, offset, offset+2, offset+3,
                offset+4, offset+6, offset+5, offset+4, offset+7, offset+6,
                offset, offset+4, offset+1, offset+1, offset+4, offset+5,
                offset+3, offset+2, offset+7, offset+2, offset+6, offset+7,
                offset, offset+3, offset+4, offset+3, offset+7, offset+4,
                offset+1, offset+5, offset+2, offset+2, offset+5, offset+6
        };

        for (int i : indices) idx.add(i);
    }
}