package com.student.smarthomeconfigurator.devices;

import com.student.smarthomeconfigurator.model.Device;
import java.util.Random;

public class MotionSensor extends Device {
    private static final Random random = new Random();
    private boolean motionDetected;
    private long lastMotionTime;
    private int detectionCount;

    public MotionSensor(String roomId) {
        super("Датчик движения", roomId, "MOTION_SENSOR");
        this.motionDetected = false;
        this.detectionCount = 0;
        this.value = "Нет движения";
    }

    public void updateValue() {
        boolean newMotion = random.nextInt(100) < 5;

        if (newMotion && !motionDetected) {
            motionDetected = true;
            lastMotionTime = System.currentTimeMillis();
            detectionCount++;
            this.value = "Движение!";
            this.status = true;
        } else if (motionDetected && System.currentTimeMillis() - lastMotionTime > 5000) {
            motionDetected = false;
            this.value = "Нет движения";
            this.status = false;
        }
    }

    public boolean isMotionDetected() { return motionDetected; }
    public int getDetectionCount() { return detectionCount; }
}