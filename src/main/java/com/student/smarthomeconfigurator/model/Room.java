package com.student.smarthomeconfigurator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {
    private String id;
    private String name;
    private double x, y, width, height;
    private int floor;
    private List<Device> devices;

    public Room(String name, double x, double y, double width, double height) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.floor = 1;
        this.devices = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getFloor() { return floor; }
    public List<Device> getDevices() { return devices; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }
    public void setFloor(int floor) { this.floor = floor; }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public void removeDevice(String deviceId) {
        devices.removeIf(d -> d.getId().equals(deviceId));
    }
}