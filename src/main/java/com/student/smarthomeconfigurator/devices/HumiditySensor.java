package com.student.smarthomeconfigurator.devices;

import com.student.smarthomeconfigurator.model.Device;
import java.util.Random;

public class HumiditySensor extends Device {
    private static final Random random = new Random();
    private double humidity;

    public HumiditySensor(String roomId) {
        super("Датчик влажности", roomId, "HUMIDITY_SENSOR");
        this.humidity = 40 + random.nextDouble() * 40;
        this.value = String.format("%.1f%%", humidity);
    }

    public void updateValue() {
        double change = (random.nextDouble() - 0.5) * 0.5;
        humidity += change;
        humidity = Math.max(20, Math.min(90, humidity));
        this.value = String.format("%.1f%%", humidity);
    }

    public double getHumidity() { return humidity; }
}