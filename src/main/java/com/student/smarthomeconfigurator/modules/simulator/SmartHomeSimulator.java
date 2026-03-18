package com.student.smarthomeconfigurator.modules.simulator;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Room;
import com.student.smarthomeconfigurator.model.Device;
import com.student.smarthomeconfigurator.devices.*;
import javafx.animation.AnimationTimer;
import java.util.*;

public class SmartHomeSimulator implements DeviceSimulator {
    private AnimationTimer timer;
    private Project currentProject;
    private Map<String, Long> lastMotionTime = new HashMap<>();

    @Override
    public void startSimulation(Project project) {
        this.currentProject = project;
        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                if ((now - lastUpdate) > 500_000_000) {
                    updateAllDevices();
                    checkAutomation();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
        System.out.println("Умный дом симуляция запущена");
    }

    private void updateAllDevices() {
        if (currentProject == null) return;

        for (Room room : currentProject.getRooms()) {
            for (Device device : room.getDevices()) {
                if (device instanceof TemperatureSensor) {
                    ((TemperatureSensor) device).updateValue();
                } else if (device instanceof HumiditySensor) {
                    ((HumiditySensor) device).updateValue();
                } else if (device instanceof LightSensor) {
                    ((LightSensor) device).updateValue();
                } else if (device instanceof MotionSensor) {
                    ((MotionSensor) device).updateValue();
                } else if (device instanceof SmokeSensor) {
                    ((SmokeSensor) device).updateValue();
                }
            }
        }
    }

    private void checkAutomation() {
        for (Room room : currentProject.getRooms()) {
            boolean motion = false;
            int lightLevel = 500;

            for (Device device : room.getDevices()) {
                if (device instanceof MotionSensor && device.isStatus()) {
                    motion = true;
                }
                if (device instanceof LightSensor) {
                    try {
                        String val = device.getValue().toString();
                        lightLevel = Integer.parseInt(val.replace(" lux", ""));
                    } catch (Exception e) {}
                }
            }

            if (motion && lightLevel < 300) {
                for (Device device : room.getDevices()) {
                    if (device instanceof Lamp) {
                        device.setStatus(true);
                    }
                }
            }
        }
    }

    @Override
    public void stopSimulation() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        System.out.println("Симуляция остановлена");
    }

    @Override
    public String getName() {
        return "Умный дом симулятор";
    }
}