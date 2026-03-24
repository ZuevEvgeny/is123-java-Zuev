package com.student.smarthomeconfigurator.model.building;

import java.util.ArrayList;
import java.util.List;

public class RoomArea {
    private String id;
    private String name;
    private List<Point2DData> vertices;
    private float floorHeight;
    private float ceilingHeight;
    private String floorTexture;
    private String wallTexture;
    private String ceilingTexture;

    public RoomArea() {
        this.id = java.util.UUID.randomUUID().toString();
        this.vertices = new ArrayList<>();
        this.floorHeight = 0.0f;
        this.ceilingHeight = 2.7f;
    }

    public RoomArea(String name) {
        this();
        this.name = name;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Point2DData> getVertices() { return vertices; }
    public void setVertices(List<Point2DData> vertices) { this.vertices = vertices; }

    public float getFloorHeight() { return floorHeight; }
    public void setFloorHeight(float floorHeight) { this.floorHeight = floorHeight; }

    public float getCeilingHeight() { return ceilingHeight; }
    public void setCeilingHeight(float ceilingHeight) { this.ceilingHeight = ceilingHeight; }

    public String getFloorTexture() { return floorTexture; }
    public void setFloorTexture(String floorTexture) { this.floorTexture = floorTexture; }

    public String getWallTexture() { return wallTexture; }
    public void setWallTexture(String wallTexture) { this.wallTexture = wallTexture; }

    public String getCeilingTexture() { return ceilingTexture; }
    public void setCeilingTexture(String ceilingTexture) { this.ceilingTexture = ceilingTexture; }

    public void addVertex(double x, double z) {
        vertices.add(new Point2DData(x, z));
    }

    public void addVertex(Point2DData vertex) {
        vertices.add(vertex);
    }
}