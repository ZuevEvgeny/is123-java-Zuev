package com.student.smarthomeconfigurator.model.building;

import java.util.ArrayList;
import java.util.List;

/**
 * Этаж здания
 */
public class Floor {
    private int level;           // 0, 1, 2...
    private float height;        // высота этажа (м)
    private float ceilingHeight; // высота потолка (м)
    private List<WallSegment> walls;
    private List<RoomArea> rooms;

    public Floor() {
        this.walls = new ArrayList<>();
        this.rooms = new ArrayList<>();
        this.height = 3.0f;
        this.ceilingHeight = 2.7f;
    }

    public Floor(int level) {
        this();
        this.level = level;
    }

    // Геттеры и сеттеры
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getCeilingHeight() { return ceilingHeight; }
    public void setCeilingHeight(float ceilingHeight) { this.ceilingHeight = ceilingHeight; }

    public List<WallSegment> getWalls() { return walls; }
    public void setWalls(List<WallSegment> walls) { this.walls = walls; }

    public List<RoomArea> getRooms() { return rooms; }
    public void setRooms(List<RoomArea> rooms) { this.rooms = rooms; }
}