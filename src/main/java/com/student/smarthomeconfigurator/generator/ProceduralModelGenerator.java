package com.student.smarthomeconfigurator.generator;

import org.joml.Matrix4f;
import java.util.*;

public class ProceduralModelGenerator {

    public static class ModelData {
        public List<float[]> verticesList = new ArrayList<>();
        public List<float[]> normalsList = new ArrayList<>();
        public List<float[]> texCoordsList = new ArrayList<>();
        public List<int[]> indicesList = new ArrayList<>();
        public List<float[]> colors = new ArrayList<>();
        public List<Matrix4f> transforms = new ArrayList<>();
    }

    public static ModelData createTable(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float tableHeight = 0.7f * scale;
        float topHeight = 0.05f * scale;
        float legHeight = tableHeight;

        addBox(data, 0, tableHeight - topHeight/2, 0, 1.2f * scale, topHeight, 0.8f * scale,
                new float[]{0.65f, 0.45f, 0.25f, 1.0f}, 0);

        float legSize = 0.08f * scale;
        float legPosY = legHeight/2;

        addBox(data, -0.55f * scale, legPosY, -0.35f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);
        addBox(data, 0.55f * scale, legPosY, -0.35f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);
        addBox(data, -0.55f * scale, legPosY, 0.35f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);
        addBox(data, 0.55f * scale, legPosY, 0.35f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);

        return data;
    }

    public static ModelData createChair(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float seatHeight = 0.45f * scale;
        float seatThick = 0.08f * scale;

        addBox(data, 0, seatHeight, 0, 0.45f * scale, seatThick, 0.45f * scale,
                new float[]{0.6f, 0.4f, 0.2f, 1.0f}, 0);

        addBox(data, 0, seatHeight + 0.2f * scale, -0.22f * scale,
                0.45f * scale, 0.4f * scale, 0.05f * scale,
                new float[]{0.6f, 0.4f, 0.2f, 1.0f}, 0);

        float legHeight = seatHeight - seatThick/2;
        float legPosY = legHeight/2;
        float legSize = 0.06f * scale;

        addBox(data, -0.18f * scale, legPosY, -0.18f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);
        addBox(data, 0.18f * scale, legPosY, -0.18f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);
        addBox(data, -0.18f * scale, legPosY, 0.18f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);
        addBox(data, 0.18f * scale, legPosY, 0.18f * scale,
                legSize, legHeight, legSize,
                new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);

        return data;
    }

    public static ModelData createDoubleBed(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float bedHeight = 0.3f * scale;

        addBox(data, 0, bedHeight/2, 0, 1.6f * scale, bedHeight, 2.0f * scale,
                new float[]{0.5f, 0.35f, 0.25f, 1.0f}, 0);

        addBox(data, 0, bedHeight + 0.05f * scale, 0, 1.55f * scale, 0.2f * scale, 1.95f * scale,
                new float[]{0.9f, 0.85f, 0.8f, 1.0f}, 0);

        addBox(data, 0, bedHeight + 0.15f * scale, -0.95f * scale,
                1.6f * scale, 0.5f * scale, 0.1f * scale,
                new float[]{0.5f, 0.35f, 0.25f, 1.0f}, 0);

        return data;
    }

    public static ModelData createWardrobe(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float cabinetHeight = 2.0f * scale;

        addBox(data, 0, cabinetHeight/2, 0, 0.9f * scale, cabinetHeight, 0.6f * scale,
                new float[]{0.55f, 0.4f, 0.25f, 1.0f}, 0);

        float doorZ = 0.31f * scale;
        addBox(data, -0.2f * scale, cabinetHeight/2, doorZ,
                0.4f * scale, cabinetHeight - 0.1f * scale, 0.03f * scale,
                new float[]{0.7f, 0.5f, 0.35f, 1.0f}, 0);
        addBox(data, 0.2f * scale, cabinetHeight/2, doorZ,
                0.4f * scale, cabinetHeight - 0.1f * scale, 0.03f * scale,
                new float[]{0.7f, 0.5f, 0.35f, 1.0f}, 0);

        addBox(data, -0.32f * scale, cabinetHeight/2, doorZ + 0.02f * scale,
                0.03f * scale, 0.08f * scale, 0.01f * scale,
                new float[]{0.9f, 0.8f, 0.7f, 1.0f}, 0);
        addBox(data, 0.08f * scale, cabinetHeight/2, doorZ + 0.02f * scale,
                0.03f * scale, 0.08f * scale, 0.01f * scale,
                new float[]{0.9f, 0.8f, 0.7f, 1.0f}, 0);

        return data;
    }

    public static ModelData createShelf(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float shelfHeight = 1.6f * scale;

        addBox(data, 0, shelfHeight/2, 0, 0.8f * scale, shelfHeight, 0.4f * scale,
                new float[]{0.6f, 0.45f, 0.3f, 1.0f}, 0);

        float[] shelfPositions = {0.5f, 0.9f, 1.3f};
        for (float pos : shelfPositions) {
            addBox(data, 0, pos * scale, 0, 0.75f * scale, 0.03f * scale, 0.35f * scale,
                    new float[]{0.65f, 0.5f, 0.35f, 1.0f}, 0);
        }

        return data;
    }

    public static ModelData createCornerSofa(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float sofaHeight = 0.45f * scale;
        float cushionHeight = 0.12f * scale;

        addBox(data, -0.45f * scale, sofaHeight/2 - 0.03f * scale, 0, 1.8f * scale, sofaHeight - 0.05f * scale, 0.8f * scale,
                new float[]{0.55f, 0.45f, 0.35f, 1.0f}, 0);

        addBox(data, 0.35f * scale, sofaHeight/2 - 0.03f * scale, 0.4f * scale,
                0.8f * scale, sofaHeight - 0.05f * scale, 1.0f * scale,
                new float[]{0.55f, 0.45f, 0.35f, 1.0f}, 0);

        addBox(data, -0.45f * scale, sofaHeight/2 + 0.02f * scale, 0, 1.8f * scale, cushionHeight, 0.75f * scale,
                new float[]{0.85f, 0.75f, 0.65f, 1.0f}, 0);
        addBox(data, 0.35f * scale, sofaHeight/2 + 0.02f * scale, 0.4f * scale,
                0.8f * scale, cushionHeight, 0.95f * scale,
                new float[]{0.85f, 0.75f, 0.65f, 1.0f}, 0);

        addBox(data, -0.45f * scale, sofaHeight/2 + 0.1f * scale, -0.38f * scale,
                1.8f * scale, 0.4f * scale, 0.1f * scale,
                new float[]{0.75f, 0.65f, 0.55f, 1.0f}, 0);
        addBox(data, 0.35f * scale, sofaHeight/2 + 0.1f * scale, 1.25f * scale,
                0.8f * scale, 0.4f * scale, 0.1f * scale,
                new float[]{0.75f, 0.65f, 0.55f, 1.0f}, 0);

        addBox(data, -1.3f * scale, sofaHeight/2, 0, 0.12f * scale, sofaHeight, 0.8f * scale,
                new float[]{0.6f, 0.5f, 0.4f, 1.0f}, 0);
        addBox(data, 0.85f * scale, sofaHeight/2, 0, 0.12f * scale, sofaHeight, 0.8f * scale,
                new float[]{0.6f, 0.5f, 0.4f, 1.0f}, 0);

        return data;
    }

    public static ModelData createCoffeeTable(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float tableHeight = 0.45f * scale;
        float tableWidth = 0.8f * scale;
        float tableDepth = 0.5f * scale;

        addBox(data, 0, tableHeight, 0, tableWidth, 0.05f * scale, tableDepth,
                new float[]{0.55f, 0.4f, 0.25f, 1.0f}, 0);

        float legSize = 0.05f * scale;
        float legPosY = tableHeight/2;

        float[] offsets = {-0.35f, 0.35f};
        for (float ox : offsets) {
            for (float oz : offsets) {
                addBox(data, ox * scale, legPosY, oz * (tableDepth/2 - 0.1f) * scale,
                        legSize, tableHeight, legSize,
                        new float[]{0.5f, 0.35f, 0.2f, 1.0f}, 0);
            }
        }

        return data;
    }

    public static ModelData createNightstand(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float height = 0.5f * scale;
        float width = 0.45f * scale;
        float depth = 0.4f * scale;

        addBox(data, 0, height/2, 0, width, height, depth,
                new float[]{0.6f, 0.45f, 0.3f, 1.0f}, 0);

        addBox(data, 0, height/3, 0.21f * scale, width - 0.05f * scale, height/2.5f, 0.02f * scale,
                new float[]{0.7f, 0.55f, 0.4f, 1.0f}, 0);

        addBox(data, -0.1f * scale, height/3, 0.22f * scale,
                0.05f * scale, 0.03f * scale, 0.02f * scale,
                new float[]{0.9f, 0.7f, 0.3f, 1.0f}, 0);

        return data;
    }

    public static ModelData createTV(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float tvHeight = 0.6f * scale;
        float tvWidth = 1.0f * scale;
        float tvDepth = 0.05f * scale;

        addBox(data, 0, tvHeight/2, 0, tvWidth, tvHeight, tvDepth,
                new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);

        addBox(data, 0, tvHeight/2, 0.01f * scale, tvWidth + 0.03f * scale, tvHeight + 0.03f * scale, 0.01f * scale,
                new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);

        addBox(data, 0, -0.05f * scale, 0, 0.4f * scale, 0.05f * scale, 0.2f * scale,
                new float[]{0.4f, 0.4f, 0.4f, 1.0f}, 0);

        return data;
    }

    public static ModelData createStove(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float stoveHeight = 0.85f * scale;
        float stoveWidth = 0.6f * scale;
        float stoveDepth = 0.6f * scale;

        addBox(data, 0, stoveHeight/2, 0, stoveWidth, stoveHeight, stoveDepth,
                new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);

        addBox(data, 0, stoveHeight - 0.05f * scale, 0, stoveWidth - 0.05f * scale, 0.03f * scale, stoveDepth - 0.05f * scale,
                new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);

        float[] offsets = {-0.18f, 0.18f};
        for (float ox : offsets) {
            for (float oz : offsets) {
                addBox(data, ox * scale, stoveHeight - 0.02f * scale, oz * scale,
                        0.12f * scale, 0.02f * scale, 0.12f * scale,
                        new float[]{0.8f, 0.6f, 0.2f, 1.0f}, 0);
            }
        }

        addBox(data, 0, stoveHeight/2 - 0.1f * scale, 0.31f * scale,
                stoveWidth - 0.1f * scale, stoveHeight/2 - 0.05f * scale, 0.03f * scale,
                new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);

        addBox(data, 0, stoveHeight/2 - 0.1f * scale, 0.33f * scale,
                0.1f * scale, 0.02f * scale, 0.02f * scale,
                new float[]{0.9f, 0.7f, 0.3f, 1.0f}, 0);

        addBox(data, 0.25f * scale, stoveHeight - 0.08f * scale, 0.31f * scale,
                0.12f * scale, 0.06f * scale, 0.02f * scale,
                new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);

        return data;
    }

    public static ModelData createFridge(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float fridgeHeight = 1.8f * scale;
        float fridgeWidth = 0.7f * scale;
        float fridgeDepth = 0.7f * scale;

        addBox(data, 0, fridgeHeight/2, 0, fridgeWidth, fridgeHeight, fridgeDepth,
                new float[]{0.95f, 0.95f, 0.95f, 1.0f}, 0);

        addBox(data, 0, fridgeHeight/2, 0.36f * scale, fridgeWidth - 0.05f * scale, fridgeHeight - 0.1f * scale, 0.03f * scale,
                new float[]{0.98f, 0.98f, 0.98f, 1.0f}, 0);

        addBox(data, -0.25f * scale, fridgeHeight/2, 0.38f * scale,
                0.05f * scale, 0.15f * scale, 0.02f * scale,
                new float[]{0.6f, 0.6f, 0.6f, 1.0f}, 0);

        addBox(data, 0, fridgeHeight - 0.35f * scale, 0.37f * scale,
                fridgeWidth - 0.1f * scale, 0.25f * scale, 0.02f * scale,
                new float[]{0.7f, 0.7f, 0.7f, 1.0f}, 0);

        return data;
    }

    public static ModelData createMicrowave(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float mwHeight = 0.35f * scale;
        float mwWidth = 0.55f * scale;
        float mwDepth = 0.45f * scale;

        addBox(data, 0, mwHeight/2, 0, mwWidth, mwHeight, mwDepth,
                new float[]{0.85f, 0.85f, 0.85f, 1.0f}, 0);

        addBox(data, 0, mwHeight/2, 0.23f * scale, mwWidth - 0.1f * scale, mwHeight - 0.1f * scale, 0.02f * scale,
                new float[]{0.3f, 0.3f, 0.3f, 0.7f}, 0);

        addBox(data, 0, mwHeight/2, 0.24f * scale, mwWidth - 0.2f * scale, mwHeight - 0.2f * scale, 0.01f * scale,
                new float[]{0.1f, 0.1f, 0.1f, 0.5f}, 0);

        addBox(data, -0.2f * scale, mwHeight/2, 0.24f * scale,
                0.08f * scale, 0.04f * scale, 0.02f * scale,
                new float[]{0.9f, 0.7f, 0.2f, 1.0f}, 0);

        addBox(data, 0.15f * scale, mwHeight - 0.05f * scale, 0.24f * scale,
                0.12f * scale, 0.08f * scale, 0.02f * scale,
                new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);

        return data;
    }

    public static ModelData createDishwasher(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float dwHeight = 0.85f * scale;
        float dwWidth = 0.6f * scale;
        float dwDepth = 0.6f * scale;

        addBox(data, 0, dwHeight/2, 0, dwWidth, dwHeight, dwDepth,
                new float[]{0.9f, 0.9f, 0.9f, 1.0f}, 0);

        addBox(data, 0, dwHeight/2, 0.31f * scale, dwWidth - 0.05f * scale, dwHeight - 0.1f * scale, 0.03f * scale,
                new float[]{0.95f, 0.95f, 0.95f, 1.0f}, 0);

        addBox(data, 0, dwHeight - 0.08f * scale, 0.32f * scale,
                dwWidth - 0.15f * scale, 0.06f * scale, 0.02f * scale,
                new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);

        addBox(data, -0.25f * scale, dwHeight/2, 0.33f * scale,
                0.05f * scale, 0.1f * scale, 0.02f * scale,
                new float[]{0.6f, 0.6f, 0.6f, 1.0f}, 0);

        return data;
    }

    public static ModelData createWashingMachine(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        float wmHeight = 0.85f * scale;
        float wmWidth = 0.6f * scale;
        float wmDepth = 0.6f * scale;

        addBox(data, 0, wmHeight/2, 0, wmWidth, wmHeight, wmDepth,
                new float[]{0.95f, 0.95f, 0.95f, 1.0f}, 0);

        addBox(data, 0, wmHeight/2, 0.31f * scale, 0.35f * scale, 0.35f * scale, 0.03f * scale,
                new float[]{0.7f, 0.8f, 0.9f, 0.8f}, 0);

        addBox(data, 0.2f * scale, wmHeight - 0.1f * scale, 0.32f * scale,
                0.2f * scale, 0.08f * scale, 0.02f * scale,
                new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);

        return data;
    }

    public static ModelData createChandelier(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0, 0, 0.12f * scale, 0.06f * scale, 0.12f * scale,
                new float[]{0.9f, 0.85f, 0.7f, 1.0f}, 0);

        addBox(data, 0, -0.12f * scale, 0, 0.06f * scale, 0.2f * scale, 0.06f * scale,
                new float[]{0.8f, 0.7f, 0.5f, 1.0f}, 0);

        addBox(data, 0, -0.28f * scale, 0, 0.18f * scale, 0.18f * scale, 0.18f * scale,
                new float[]{1.0f, 0.85f, 0.6f, 1.0f}, 0);

        addBox(data, 0, -0.45f * scale, 0, 0.35f * scale, 0.2f * scale, 0.35f * scale,
                new float[]{0.95f, 0.85f, 0.7f, 1.0f}, 0);

        for (int i = 0; i < 4; i++) {
            float angleRad = (float) (i * Math.PI / 2);
            float armX = (float)Math.cos(angleRad) * 0.35f * scale;
            float armZ = (float)Math.sin(angleRad) * 0.35f * scale;

            addBox(data, armX, -0.38f * scale, armZ, 0.08f * scale, 0.12f * scale, 0.08f * scale,
                    new float[]{0.85f, 0.75f, 0.6f, 1.0f}, 0);

            addBox(data, armX, -0.48f * scale, armZ, 0.12f * scale, 0.12f * scale, 0.12f * scale,
                    new float[]{1.0f, 0.95f, 0.8f, 1.0f}, 0);
        }

        return data;
    }

    public static ModelData createDeskLamp(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0.015f * scale, 0, 0.12f * scale, 0.03f * scale, 0.12f * scale,
                new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);

        addBox(data, 0, 0.185f * scale, 0, 0.03f * scale, 0.35f * scale, 0.03f * scale,
                new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);

        addBox(data, 0, 0.365f * scale, 0, 0.2f * scale, 0.12f * scale, 0.2f * scale,
                new float[]{1.0f, 0.9f, 0.7f, 1.0f}, 0);

        addBox(data, 0, 0.305f * scale, 0, 0.06f * scale, 0.08f * scale, 0.06f * scale,
                new float[]{1.0f, 0.98f, 0.9f, 1.0f}, 0);

        return data;
    }

    public static ModelData createPendantLamp(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, -0.2f * scale, 0, 0.02f * scale, 0.4f * scale, 0.02f * scale,
                new float[]{0.3f, 0.3f, 0.3f, 1.0f}, 0);

        addBox(data, 0, -0.5f * scale, 0, 0.25f * scale, 0.2f * scale, 0.25f * scale,
                new float[]{1.0f, 0.9f, 0.7f, 1.0f}, 0);

        addBox(data, 0, -0.65f * scale, 0, 0.08f * scale, 0.1f * scale, 0.08f * scale,
                new float[]{1.0f, 0.98f, 0.9f, 1.0f}, 0);

        return data;
    }

    public static ModelData createFloorLamp(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0.02f * scale, 0, 0.25f * scale, 0.04f * scale, 0.25f * scale,
                new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);

        addBox(data, 0, 0.7f * scale, 0, 0.04f * scale, 1.3f * scale, 0.04f * scale,
                new float[]{0.4f, 0.4f, 0.4f, 1.0f}, 0);

        addBox(data, 0, 1.35f * scale, 0, 0.35f * scale, 0.25f * scale, 0.35f * scale,
                new float[]{1.0f, 0.95f, 0.85f, 1.0f}, 0);

        addBox(data, 0, 1.2f * scale, 0, 0.1f * scale, 0.12f * scale, 0.1f * scale,
                new float[]{1.0f, 0.98f, 0.9f, 1.0f}, 0);

        return data;
    }

    public static ModelData createSpotlight(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0, 0, 0.12f * scale, 0.03f * scale, 0.12f * scale,
                new float[]{0.8f, 0.8f, 0.8f, 1.0f}, 0);

        addBox(data, 0, -0.08f * scale, 0, 0.14f * scale, 0.12f * scale, 0.14f * scale,
                new float[]{0.4f, 0.4f, 0.4f, 1.0f}, 0);

        addBox(data, 0, -0.15f * scale, 0, 0.08f * scale, 0.08f * scale, 0.08f * scale,
                new float[]{1.0f, 0.95f, 0.8f, 1.0f}, 0);

        return data;
    }

    public static ModelData createMotionSensor(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0, 0.025f * scale, 0.1f * scale, 0.08f * scale, 0.05f * scale,
                new float[]{0.95f, 0.95f, 0.95f, 1.0f}, 0);

        addBox(data, 0, 0, 0.05f * scale, 0.06f * scale, 0.04f * scale, 0.02f * scale,
                new float[]{0.2f, 0.2f, 0.3f, 1.0f}, 0);

        return data;
    }

    public static ModelData createTemperatureSensor(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0, 0.02f * scale, 0.08f * scale, 0.08f * scale, 0.04f * scale,
                new float[]{0.95f, 0.95f, 0.95f, 1.0f}, 0);

        addBox(data, 0, 0.02f * scale, 0.04f * scale, 0.03f * scale, 0.02f * scale, 0.01f * scale,
                new float[]{0.0f, 1.0f, 0.0f, 1.0f}, 0);

        return data;
    }

    public static ModelData createSmokeSensor(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0, 0, 0.12f * scale, 0.04f * scale, 0.12f * scale,
                new float[]{0.95f, 0.95f, 0.95f, 1.0f}, 0);

        addBox(data, 0, -0.03f * scale, 0, 0.1f * scale, 0.04f * scale, 0.1f * scale,
                new float[]{0.8f, 0.8f, 0.8f, 1.0f}, 0);

        addBox(data, 0, -0.05f * scale, 0, 0.04f * scale, 0.02f * scale, 0.04f * scale,
                new float[]{1.0f, 0.0f, 0.0f, 1.0f}, 0);

        return data;
    }

    public static ModelData createLightSensor(float x, float y, float z, float scale, float rotation) {
        ModelData data = new ModelData();

        addBox(data, 0, 0, 0.015f * scale, 0.08f * scale, 0.06f * scale, 0.03f * scale,
                new float[]{0.9f, 0.9f, 0.9f, 1.0f}, 0);

        addBox(data, 0, 0.01f * scale, 0.03f * scale, 0.04f * scale, 0.02f * scale, 0.01f * scale,
                new float[]{0.3f, 0.3f, 0.5f, 1.0f}, 0);

        return data;
    }



    private static void addBox(ModelData data, float x, float y, float z,
                               float width, float height, float depth,
                               float[] color, float rotation) {
        List<Float> verts = new ArrayList<>();
        List<Float> norms = new ArrayList<>();
        List<Float> texs = new ArrayList<>();
        List<Integer> idxs = new ArrayList<>();

        float halfW = width / 2.0f;
        float halfH = height / 2.0f;
        float halfD = depth / 2.0f;

        float[][] vertices = {
                {-halfW, -halfH, -halfD}, {halfW, -halfH, -halfD},
                {halfW,  halfH, -halfD}, {-halfW,  halfH, -halfD},
                {-halfW, -halfH,  halfD}, {halfW, -halfH,  halfD},
                {halfW,  halfH,  halfD}, {-halfW,  halfH,  halfD}
        };

        for (float[] v : vertices) {
            float rx = (float)(v[0] * Math.cos(rotation) - v[2] * Math.sin(rotation));
            float ry = v[1];
            float rz = (float)(v[0] * Math.sin(rotation) + v[2] * Math.cos(rotation));

            verts.add(x + rx);
            verts.add(y + ry);
            verts.add(z + rz);
        }

        float[][] faceNormals = {
                {0, 0, -1}, {0, 0, 1},
                {-1, 0, 0}, {1, 0, 0},
                {0, -1, 0}, {0, 1, 0}
        };

        for (float[] fn : faceNormals) {
            for (int i = 0; i < 4; i++) {
                norms.add(fn[0]);
                norms.add(fn[1]);
                norms.add(fn[2]);
            }
        }

        float[] texCoords = {0,0, 1,0, 1,1, 0,1};
        for (int i = 0; i < 6; i++) {
            for (float tc : texCoords) {
                texs.add(tc);
            }
        }

        int[][] indices = {
                {0,1,2, 0,2,3}, {4,6,5, 4,7,6},
                {0,4,1, 1,4,5}, {3,2,7, 2,6,7},
                {0,3,4, 3,7,4}, {1,5,2, 2,5,6}
        };

        int baseIndex = verts.size() / 3 - 8;
        for (int[] idx : indices) {
            for (int i : idx) {
                idxs.add(baseIndex + i);
            }
        }

        addMeshPart(data, verts, norms, texs, idxs, color);
    }

    private static void addMeshPart(ModelData data, List<Float> verts, List<Float> norms,
                                    List<Float> texs, List<Integer> idxs, float[] color) {
        float[] vArray = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) vArray[i] = verts.get(i);

        float[] nArray = new float[norms.size()];
        for (int i = 0; i < norms.size(); i++) nArray[i] = norms.get(i);

        float[] tArray = new float[texs.size()];
        for (int i = 0; i < texs.size(); i++) tArray[i] = texs.get(i);

        int[] iArray = new int[idxs.size()];
        for (int i = 0; i < idxs.size(); i++) iArray[i] = idxs.get(i);

        data.verticesList.add(vArray);
        data.normalsList.add(nArray);
        data.texCoordsList.add(tArray);
        data.indicesList.add(iArray);
        data.colors.add(color);
        data.transforms.add(new Matrix4f().identity());
    }
}