package ru.nsu.diagrameditor.model;

import java.util.ArrayList;
import java.util.List;

public class Diagram {

    private String name;
    private String filePath;
    private final List<Component> components = new ArrayList<>();
    private ComponentSet componentSet;
    private Schema schema;
    private EditMode mode;

    public Diagram(ComponentSet componentSet, Schema schema) {
        this.componentSet = componentSet;
        setSchema(schema);
    }

    public void addComponent(Component component) {
        components.add(component);
    }

    public void removeComponent(Component component) {
        components.remove(component);
    }

    public List<Component> getComponents() {
        return List.copyOf(components);
    }

    public EditMode getMode() {
        return mode;
    }

    public ComponentSet getComponentSet() {
        return componentSet;
    }

    public void setComponentSet(ComponentSet componentSet) {
        this.componentSet = componentSet;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
        this.mode = schema == null ? EditMode.FREE : EditMode.STRICT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
