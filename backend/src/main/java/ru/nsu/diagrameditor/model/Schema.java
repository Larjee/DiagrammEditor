package ru.nsu.diagrameditor.model;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;

public class Schema {

    private final String name;
    private final List<String> allowedBlockTypes;
    private final List<String> allowedConnectorTypes;
    private final Map<String, Integer> maxPorts;
    private final List<ConnectionRule> connectionRules;
    private final Map<String, Dimension> sizeLimits;
    private final Map<String, List<Style>> allowedStyles;

    public Schema(String name,
                  List<String> allowedBlockTypes,
                  List<String> allowedConnectorTypes,
                  Map<String, Integer> maxPorts,
                  List<ConnectionRule> connectionRules,
                  Map<String, Dimension> sizeLimits,
                  Map<String, List<Style>> allowedStyles) {
        this.name = name;
        this.allowedBlockTypes = List.copyOf(allowedBlockTypes);
        this.allowedConnectorTypes = List.copyOf(allowedConnectorTypes);
        this.maxPorts = Map.copyOf(maxPorts);
        this.connectionRules = List.copyOf(connectionRules);
        this.sizeLimits = Map.copyOf(sizeLimits);
        this.allowedStyles = Map.copyOf(allowedStyles);
    }

    public boolean isBlockAllowed(String blockType) {
        return allowedBlockTypes.contains(blockType);
    }

    public boolean isConnectionAllowed(String connectorType, String sourceBlockType, String targetBlockType) {
        return allowedConnectorTypes.contains(connectorType)
                && connectionRules.contains(new ConnectionRule(connectorType, sourceBlockType, targetBlockType));
    }

    public int getMaxPorts(String blockType) {
        return maxPorts.getOrDefault(blockType, Integer.MAX_VALUE);
    }

    public boolean isSizeAllowed(String type, double width, double height) {
        Dimension limit = sizeLimits.get(type);
        return limit == null || (width <= limit.getWidth() && height <= limit.getHeight());
    }

    public boolean isStyleAllowed(String type, Style style) {
        List<Style> styles = allowedStyles.get(type);
        return styles == null || styles.contains(style);
    }

    public String getName() {
        return name;
    }
}
