package com.student.smarthomeconfigurator.modules.simulator;

import com.student.smarthomeconfigurator.model.Project;

public interface DeviceSimulator {
    void startSimulation(Project project);
    void stopSimulation();
    String getName();
}