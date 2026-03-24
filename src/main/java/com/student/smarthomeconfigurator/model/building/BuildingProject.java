package com.student.smarthomeconfigurator.model.building;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildingProject {
    private String id;
    private String name;
    private List<Floor> floors;
    private List<WallSegment> walls;
    private List<RoomArea> rooms;
    private List<FurnitureItem> furniture;
    private CameraState camera;
    private long createdAt;
    private long modifiedAt;

    public BuildingProject() {
        this.id = UUID.randomUUID().toString();
        this.floors = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.rooms = new ArrayList<>();
        this.furniture = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.modifiedAt = System.currentTimeMillis();
    }

    public BuildingProject(String name) {
        this();
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Floor> getFloors() { return floors; }
    public void setFloors(List<Floor> floors) { this.floors = floors; }

    public List<WallSegment> getWalls() { return walls; }
    public void setWalls(List<WallSegment> walls) { this.walls = walls; }

    public List<RoomArea> getRooms() { return rooms; }
    public void setRooms(List<RoomArea> rooms) { this.rooms = rooms; }

    public List<FurnitureItem> getFurniture() { return furniture; }
    public void setFurniture(List<FurnitureItem> furniture) { this.furniture = furniture; }

    public CameraState getCamera() { return camera; }
    public void setCamera(CameraState camera) { this.camera = camera; }

    public long getCreatedAt() { return createdAt; }
    public long getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(long modifiedAt) { this.modifiedAt = modifiedAt; }
}