package com.student.smarthomeconfigurator.devices;

import com.student.smarthomeconfigurator.model.Device;
import java.util.Random;

public class TemperatureSensor extends Device {
    private static final Random random = new Random();
    private double currentTemp;
    private double targetTemp = 22.0;
    private double trend = 0;

    public TemperatureSensor(String roomId) {
        super("Датчик температуры", roomId, "TEMPERATURE_SENSOR");
        this.currentTemp = 20.0 + random.nextDouble() * 5;
        this.value = String.format("%.1f°C", currentTemp);
    }

    public void updateValue() {
        double change = (random.nextDouble() - 0.5) * 0.3;
        trend += (random.nextDouble() - 0.5) * 0.1;
        trend = Math.max(-0.2, Math.min(0.2, trend));

        currentTemp += change + trend;
        currentTemp = Math.max(18, Math.min(28, currentTemp));

        this.value = String.format("%.1f°C", currentTemp);
        notifyListeners();
    }

    public double getCurrentTemp() { return currentTemp; }
}