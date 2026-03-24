package com.student.smarthomeconfigurator.model.building;

import java.io.Serializable;

public class Point2DData implements Serializable {
    public double x;
    public double y;

    public Point2DData() {}

    public Point2DData(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}