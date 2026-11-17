package ru.nsu.diagrameditor.model;

import java.util.UUID;

public abstract class Component {

    private final String id;
    private final String type;
    private Style style;

    protected Component(String type, Style style) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.style = style;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public abstract void resize(double width, double height);
}
