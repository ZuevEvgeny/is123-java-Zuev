package com.student.smarthomeconfigurator.modules.renderer;

import com.student.smarthomeconfigurator.model.Project;
import javafx.scene.layout.Pane;

public interface MapRenderer {
    void render(Project project, Pane container);
    void clear();
    String getName();
    void handleClick(double x, double y);
}