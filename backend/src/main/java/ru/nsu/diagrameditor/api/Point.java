package ru.nsu.diagrameditor.api;

import ru.nsu.diagrameditor.model.InflectionPoint;

public record Point(double x, double y) {

    static Point from(InflectionPoint point) {
        return new Point(point.getX(), point.getY());
    }
}
