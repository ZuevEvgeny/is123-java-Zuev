package com.student.smarthomeconfigurator.modules.renderer;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Room;
import javafx.scene.layout.Pane;
import java.io.FileWriter;
import java.io.IOException;

public class SvgExporter implements MapRenderer {
    private Project currentProject;

    @Override
    public void render(Project project, Pane container) {
        this.currentProject = project;
    }

    public void exportToFile(String filePath) {
        if (currentProject == null) {
            System.out.println("Нет проекта для экспорта");
            return;
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

        for (Room room : currentProject.getRooms()) {
            svg.append(String.format("  <rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" " +
                            "fill=\"lightblue\" stroke=\"black\"/>\n",
                    room.getX(), room.getY(), room.getWidth(), room.getHeight()));
            svg.append(String.format("  <text x=\"%f\" y=\"%f\" fill=\"black\">%s</text>\n",
                    room.getX() + 5, room.getY() + 20, room.getName()));
        }

        svg.append("</svg>");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(svg.toString());
            System.out.println("SVG экспортирован в: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {}

    @Override
    public String getName() {
        return "SVG Exporter";
    }

    @Override
    public void handleClick(double x, double y) {}
}