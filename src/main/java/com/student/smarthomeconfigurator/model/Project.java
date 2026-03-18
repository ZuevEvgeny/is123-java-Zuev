package com.student.smarthomeconfigurator.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Project {
    private String id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<Room> rooms;

    public Project(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.rooms = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public List<Room> getRooms() { return rooms; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) {
        this.name = name;
        this.modifiedAt = LocalDateTime.now();
    }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }

    public void addRoom(Room room) {
        rooms.add(room);
        this.modifiedAt = LocalDateTime.now();
    }

    public void removeRoom(String roomId) {
        rooms.removeIf(r -> r.getId().equals(roomId));
        this.modifiedAt = LocalDateTime.now();
    }
}