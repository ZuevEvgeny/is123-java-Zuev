package com.student.smarthomeconfigurator.modules.renderer;

import com.student.smarthomeconfigurator.model.Project;
import com.student.smarthomeconfigurator.model.Room;
import com.student.smarthomeconfigurator.model.Device;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Canvas2DRenderer implements MapRenderer {
    private Canvas canvas;
    private GraphicsContext gc;
    private Project currentProject;

    @Override
    public void render(Project project, Pane container) {
        this.currentProject = project;
        container.getChildren().clear();

        if (canvas == null) {
            canvas = new Canvas();
            canvas.widthProperty().bind(container.widthProperty());
            canvas.heightProperty().bind(container.heightProperty());
            container.getChildren().add(canvas);
            gc = canvas.getGraphicsContext2D();
            canvas.setOnMouseClicked(event -> handleClick(event.getX(), event.getY()));
        }

        draw();
    }

    private void draw() {
        if (gc == null || canvas.getWidth() == 0) return;

        // Фон
        gc.setFill(Color.rgb(240, 248, 255));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Сетка
        gc.setStroke(Color.rgb(200, 200, 220));
        gc.setLineWidth(0.5);
        for (int x = 0; x < canvas.getWidth(); x += 50) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        for (int y = 0; y < canvas.getHeight(); y += 50) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }

        if (currentProject != null) {
            for (Room room : currentProject.getRooms()) {
                drawRoom(room);
            }
        }
    }

    private void drawRoom(Room room) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(room.getX(), room.getY(), room.getWidth(), room.getHeight());

        gc.setFill(Color.rgb(173, 216, 230, 0.4));
        gc.fillRect(room.getX(), room.getY(), room.getWidth(), room.getHeight());

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(14));
        gc.fillText(room.getName(), room.getX() + 10, room.getY() + 25);

        int i = 0;
        for (Device device : room.getDevices()) {
            double x = room.getX() + 30 + (i * 50);
            double y = room.getY() + 50;

            if ("LAMP".equals(device.getType())) {
                gc.setFill(device.isStatus() ? Color.YELLOW : Color.GRAY);
            } else if ("LIGHT_SENSOR".equals(device.getType())) {
                gc.setFill(Color.CYAN);
            } else if ("TEMPERATURE_SENSOR".equals(device.getType())) {
                gc.setFill(Color.ORANGE);
            } else {
                gc.setFill(Color.PURPLE);
            }

            gc.fillOval(x, y, 30, 30);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(x, y, 30, 30);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(10));
            gc.fillText(device.getType().substring(0, 3), x + 5, y + 20);

            i++;
        }
    }

    @Override
    public void clear() {
        if (gc != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
    }

    @Override
    public String getName() {
        return "2D Canvas";
    }

    @Override
    public void handleClick(double x, double y) {
        if (currentProject == null) return;
        for (Room room : currentProject.getRooms()) {
            if (x >= room.getX() && x <= room.getX() + room.getWidth() &&
                    y >= room.getY() && y <= room.getY() + room.getHeight()) {
                System.out.println("🖱️ Клик: " + room.getName());
                break;
            }
        }
    }
}