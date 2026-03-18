package com.student.smarthomeconfigurator.modules.simulator;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Device;
import com.student.smarthomeconfigurator.devices.Lamp;
import javafx.scene.shape.Sphere;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

public class ManualSimulator implements DeviceSimulator {
    private Project currentProject;
    private boolean simulationActive = false;

    @Override
    public void startSimulation(Project project) {
        this.currentProject = project;
        this.simulationActive = true;
        System.out.println("Ручное управление запущено");
    }

    public void handleDeviceClick(Device device, Sphere lampSphere) {
        if (!simulationActive) return;

        if (device instanceof Lamp) {
            Lamp lamp = (Lamp) device;
            lamp.toggle(); // Включаем/выключаем

            PhongMaterial material = new PhongMaterial();
            if (lamp.isStatus()) {
                material.setDiffuseColor(Color.YELLOW);
                material.setDiffuseColor(Color.ORANGE); // Свечение!
            } else {
                material.setDiffuseColor(Color.GRAY);
                material.setDiffuseColor(Color.BLACK);
            }
            lampSphere.setMaterial(material);

            System.out.println("Лампа " + (lamp.isStatus() ? "включена" : "выключена"));
        }
    }

    @Override
    public void stopSimulation() {
        this.simulationActive = false;
        System.out.println("Ручное управление остановлено");
    }

    @Override
    public String getName() {
        return "Ручное управление";
    }
}