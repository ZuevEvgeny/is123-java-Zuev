package com.student.smarthomeconfigurator.library;

import java.util.HashMap;
import java.util.Map;

public class LibraryItem {
    private String id;
    private String name;
    private ItemCategory category;
    private String modelPath;
    private float defaultScale;
    private float defaultYOffset;
    private String thumbnail;
    private float width;   // ширина (м)
    private float depth;   // глубина (м)
    private float height;  // высота (м)
    private Map<String, Object> defaultParams;

    public LibraryItem(String id, String name, ItemCategory category,
                       String modelPath, float defaultScale, float defaultYOffset,
                       String thumbnail, float width, float depth, float height) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.modelPath = modelPath;
        this.defaultScale = defaultScale;
        this.defaultYOffset = defaultYOffset;
        this.thumbnail = thumbnail;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.defaultParams = new HashMap<>();

        if (category == ItemCategory.LAMP) {
            defaultParams.put("intensity", 1.0f);
            defaultParams.put("color", new float[]{1.0f, 0.95f, 0.8f});
            defaultParams.put("radius", 4.0f);
        }
    }

    // Геттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public ItemCategory getCategory() { return category; }
    public String getModelPath() { return modelPath; }
    public float getDefaultScale() { return defaultScale; }
    public float getDefaultYOffset() { return defaultYOffset; }
    public String getThumbnail() { return thumbnail; }
    public float getWidth() { return width; }
    public float getDepth() { return depth; }
    public float getHeight() { return height; }
    public Map<String, Object> getDefaultParams() { return defaultParams; }
}