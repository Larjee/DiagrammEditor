package ru.nsu.diagrameditor.model;

import java.util.ArrayList;
import java.util.List;

public class Block extends Component {

    public static final double DEFAULT_WIDTH = 120;
    public static final double DEFAULT_HEIGHT = 60;

    private double x;
    private double y;
    private double width = DEFAULT_WIDTH;
    private double height = DEFAULT_HEIGHT;
    private String label;
    private final List<Port> ports = new ArrayList<>();

    public Block(String type, double x, double y, Style style) {
        super(type, style);
        this.x = x;
        this.y = y;
    }

    public void moveTo(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void resize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
