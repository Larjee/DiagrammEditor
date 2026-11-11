package ru.nsu.diagrameditor.model;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

public class ComponentSet {

    private static final Style DEFAULT_STYLE = new Style(
            Color.BLACK, 1, Color.WHITE, new Font(Font.SANS_SERIF, Font.PLAIN, 12), LineType.SOLID);

    private final String name;
    private final List<String> blockTypes;
    private final List<String> connectorTypes;

    public ComponentSet(String name, List<String> blockTypes, List<String> connectorTypes) {
        this.name = name;
        this.blockTypes = List.copyOf(blockTypes);
        this.connectorTypes = List.copyOf(connectorTypes);
    }

    public String getName() {
        return name;
    }

    public List<String> getBlockTypes() {
        return blockTypes;
    }

    public List<String> getConnectorTypes() {
        return connectorTypes;
    }

    public Style getDefaultStyle(String type) {
        return DEFAULT_STYLE;
    }
}
