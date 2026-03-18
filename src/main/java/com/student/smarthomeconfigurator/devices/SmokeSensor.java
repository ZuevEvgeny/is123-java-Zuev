package com.student.smarthomeconfigurator.devices;

import com.student.smarthomeconfigurator.model.Device;
import java.util.Random;

public class SmokeSensor extends Device {
    private static final Random random = new Random();
    private int smokeLevel;
    private boolean alarm;

    public SmokeSensor(String roomId) {
        super("Датчик дыма", roomId, "SMOKE_SENSOR");
        this.smokeLevel = 0;
        this.alarm = false;
        this.value = "0 ppm";
    }

    public void updateValue() {
        int oldLevel = smokeLevel;
        smokeLevel = Math.max(0, smokeLevel + random.nextInt(11) - 5);

        if (smokeLevel > 200 && !alarm) {
            alarm = true;
            this.status = true;
        } else if (smokeLevel <= 50 && alarm) {
            alarm = false;
            this.status = false;
        }

        this.value = smokeLevel + " ppm";
        if (alarm) {
            this.value += "ТРЕВОГА!";
        }
    }

    public boolean isAlarm() { return alarm; }
    public int getSmokeLevel() { return smokeLevel; }
}