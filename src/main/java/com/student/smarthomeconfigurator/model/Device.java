package com.student.smarthomeconfigurator.model;

import java.util.UUID;

public abstract class Device {
    protected String id;
    protected String name;
    protected String roomId;
    protected String type;
    protected boolean status;
    protected Object value;

    private java.util.List<DeviceListener> listeners = new java.util.ArrayList<>();

    public Device() {
        this.id = UUID.randomUUID().toString();
        this.status = true;
    }

    public Device(String name, String roomId, String type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.roomId = roomId;
        this.type = type;
        this.status = true;
    }

    public void addListener(DeviceListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DeviceListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners() {
        for (DeviceListener listener : listeners) {
            listener.onDeviceChanged(this);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRoomId() { return roomId; }
    public String getType() { return type; }
    public boolean isStatus() { return status; }
    public Object getValue() { return value; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setType(String type) { this.type = type; }

    public void setStatus(boolean status) {
        this.status = status;
        notifyListeners();
    }

    public void setValue(Object value) {
        this.value = value;
        notifyListeners();
    }

    public String getDeviceInfo() {
        return name + " [" + type + "]: " + (status ? "ВКЛ" : "ВЫКЛ") +
                ", значение=" + value;
    }

    public interface DeviceListener {
        void onDeviceChanged(Device device);
    }
}