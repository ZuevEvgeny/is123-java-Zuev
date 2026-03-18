package com.student.smarthomeconfigurator.devices;

import com.student.smarthomeconfigurator.model.Device;

public class Lamp extends Device {
    private int brightness;
    private String color;
    private boolean isSmart;
    private float lightRadius;
    private float[] lightColor;

    public Lamp(String roomId) {
        super("Лампа", roomId, "LAMP");
        this.brightness = 100;
        this.color = "#FFFFFF";
        this.isSmart = false;
        this.lightRadius = 8.0f;
        this.lightColor = new float[]{1.0f, 0.9f, 0.7f};
        this.value = "ON";
        this.status = true;
    }

    public Lamp(String roomId, boolean isSmart) {
        super(isSmart ? "Умная лампа" : "Лампа", roomId, isSmart ? "SMART_LAMP" : "LAMP");
        this.brightness = 100;
        this.color = "#FFFFFF";
        this.isSmart = isSmart;
        this.lightRadius = isSmart ? 12.0f : 8.0f;
        this.lightColor = new float[]{1.0f, 0.9f, 0.7f};
        this.value = "ON";
        this.status = true;
    }

    public void toggle() {
        this.status = !this.status;
        this.value = this.status ? "ON" : "OFF";
        notifyListeners();
    }

    public void setBrightness(int brightness) {
        if (isSmart) {
            this.brightness = Math.max(0, Math.min(100, brightness));
            float warmth = 1.0f - (brightness / 200.0f);
            lightColor[0] = 1.0f;
            lightColor[1] = 0.9f + warmth * 0.1f;
            lightColor[2] = 0.8f + warmth * 0.2f;
            this.value = brightness + "%";
            notifyListeners();
        }
    }

    public void setColor(String color) {
        if (isSmart) {
            this.color = color;
            try {
                int r = Integer.parseInt(color.substring(1, 3), 16);
                int g = Integer.parseInt(color.substring(3, 5), 16);
                int b = Integer.parseInt(color.substring(5, 7), 16);
                lightColor[0] = r / 255.0f;
                lightColor[1] = g / 255.0f;
                lightColor[2] = b / 255.0f;
            } catch (Exception e) {
                lightColor = new float[]{1.0f, 1.0f, 1.0f};
            }
            notifyListeners();
        }
    }

    public float[] getLightColor() {
        if (!status) return new float[]{0, 0, 0};
        // Усиливаем цвет для большей видимости
        return new float[]{
                lightColor[0] * 1.5f,
                lightColor[1] * 1.5f,
                lightColor[2] * 1.5f
        };
    }

    public float getLightRadius() { return lightRadius; }
    public int getBrightness() { return brightness; }
    public String getColor() { return color; }
    public boolean isSmart() { return isSmart; }
}