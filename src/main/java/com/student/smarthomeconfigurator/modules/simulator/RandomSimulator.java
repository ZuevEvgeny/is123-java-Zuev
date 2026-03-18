package com.student.smarthomeconfigurator.modules.simulator;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Room;
import com.student.smarthomeconfigurator.model.Device;
import com.student.smarthomeconfigurator.devices.*;
import javafx.animation.AnimationTimer;

public class RandomSimulator implements DeviceSimulator {
    private AnimationTimer timer;
    private Project currentProject;

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
                if ((now - lastUpdate) > 1_000_000_000) {
                    updateAllDevices();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
        System.out.println("Случайная симуляция запущена");
    }

    private void updateAllDevices() {
        if (currentProject == null) return;
        for (Room room : currentProject.getRooms()) {
            for (Device device : room.getDevices()) {
                if (device instanceof LightSensor) {
                    ((LightSensor) device).updateValue();
                } else if (device instanceof TemperatureSensor) {
                    ((TemperatureSensor) device).updateValue();
                } else if (device instanceof HumiditySensor) {
                    ((HumiditySensor) device).updateValue();
                } else if (device instanceof MotionSensor) {
                    ((MotionSensor) device).updateValue();
                } else if (device instanceof SmokeSensor) {
                    ((SmokeSensor) device).updateValue();
                } else if (device instanceof Lamp) {
                    if (Math.random() < 0.1) {
                        ((Lamp) device).toggle();
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
    }

    @Override
    public String getName() {
        return "Случайная симуляция";
    }
}