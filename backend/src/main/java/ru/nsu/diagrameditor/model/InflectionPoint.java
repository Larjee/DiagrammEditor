package ru.nsu.diagrameditor.model;

public class InflectionPoint {

    private double x;
    private double y;

    public InflectionPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
