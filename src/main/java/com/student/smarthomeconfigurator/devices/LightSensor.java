package com.student.smarthomeconfigurator.devices;

import com.student.smarthomeconfigurator.model.Device;
import java.util.Random;

public class LightSensor extends Device {
    private static final Random random = new Random();
    private int lux;

    public LightSensor(String roomId) {
        super("Датчик освещения", roomId, "LIGHT_SENSOR");
        this.lux = random.nextInt(1000);
        this.value = lux + " lux";
    }

    public void updateValue() {
        int timeOfDay = java.time.LocalTime.now().getHour();
        int baseLight;

        if (timeOfDay >= 6 && timeOfDay <= 18) {
            baseLight = 500 + random.nextInt(500);
        } else {
            baseLight = 50 + random.nextInt(100);
        }

        int variation = random.nextInt(100) - 50;
        int newValue = Math.max(0, baseLight + variation);

        this.lux = newValue;
        this.value = newValue + " lux";
        notifyListeners();
    }

    public int getLux() { return lux; }
}