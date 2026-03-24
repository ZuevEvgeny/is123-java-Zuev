package com.student.smarthomeconfigurator.model.building;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WallSegment {
    public enum WallType {
        OUTER, INNER, WINDOW, DOOR
    }

    private float x1, z1;
    private float x2, z2;
    private float height;
    private float thickness;
    private WallType type;
    private String texture;
    private float openingWidth;
    private float openingHeight;

    // Временные поля для вычислений (не сохраняем)
    @JsonIgnore
    private float angle;
    @JsonIgnore
    private float length;
    @JsonIgnore
    private float centerX;
    @JsonIgnore
    private float centerZ;

    public WallSegment() {
        this.height = 3.0f;
        this.thickness = 0.2f;
        this.type = WallType.INNER;
        this.openingWidth = 0.8f;
        this.openingHeight = 2.0f;
    }

    public WallSegment(float x1, float z1, float x2, float z2) {
        this();
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
    }

    // Геттеры
    public float getX1() { return x1; }
    public float getZ1() { return z1; }
    public float getX2() { return x2; }
    public float getZ2() { return z2; }
    public float getHeight() { return height; }
    public float getThickness() { return thickness; }
    public WallType getType() { return type; }
    public String getTexture() { return texture; }
    public float getOpeningWidth() { return openingWidth; }
    public float getOpeningHeight() { return openingHeight; }

    // Сеттеры
    public void setX1(float x1) { this.x1 = x1; }
    public void setZ1(float z1) { this.z1 = z1; }
    public void setX2(float x2) { this.x2 = x2; }
    public void setZ2(float z2) { this.z2 = z2; }
    public void setHeight(float height) { this.height = height; }
    public void setThickness(float thickness) { this.thickness = thickness; }
    public void setType(WallType type) { this.type = type; }
    public void setTexture(String texture) { this.texture = texture; }
    public void setOpeningWidth(float openingWidth) { this.openingWidth = openingWidth; }
    public void setOpeningHeight(float openingHeight) { this.openingHeight = openingHeight; }

    // Вычисляемые поля (не сохраняются)
    @JsonIgnore
    public float getLength() {
        return (float) Math.hypot(x2 - x1, z2 - z1);
    }

    @JsonIgnore
    public float getAngle() {
        return (float) Math.atan2(z2 - z1, x2 - x1);
    }

    @JsonIgnore
    public float getCenterX() {
        return (x1 + x2) / 2;
    }

    @JsonIgnore
    public float getCenterZ() {
        return (z1 + z2) / 2;
    }
}