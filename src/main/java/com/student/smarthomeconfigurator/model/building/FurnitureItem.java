package com.student.smarthomeconfigurator.model.building;

import java.util.HashMap;
import java.util.Map;

/**
 * Предмет интерьера (мебель, лампа, датчик)
 */
public class FurnitureItem {
    private String id;
    private String libraryId;      // ID из библиотеки моделей
    private String name;
    private String category;       // furniture, lamp, sensor
    private float x, y, z;         // позиция
    private float scale;
    private float rotation;        // угол поворота вокруг Y
    private String deviceId;       // если это устройство (лампа, датчик)
    private Map<String, Object> params; // дополнительные параметры

    public FurnitureItem() {
        this.id = java.util.UUID.randomUUID().toString();
        this.scale = 1.0f;
        this.params = new HashMap<>();
    }

    public FurnitureItem(String libraryId, String name, String category) {
        this();
        this.libraryId = libraryId;
        this.name = name;
        this.category = category;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLibraryId() { return libraryId; }
    public void setLibraryId(String libraryId) { this.libraryId = libraryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getZ() { return z; }
    public void setZ(float z) { this.z = z; }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }

    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public void setParam(String key, Object value) {
        params.put(key, value);
    }

    public Object getParam(String key) {
        return params.get(key);
    }
}