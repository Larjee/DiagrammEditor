package ru.nsu.diagrameditor.repository;

import ru.nsu.diagrameditor.model.ConnectionRule;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.model.Style;
import ru.nsu.diagrameditor.storage.DiagramFile.StyleData;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SchemaFile(
        List<String> allowedBlockTypes,
        List<String> allowedConnectorTypes,
        Map<String, Integer> maxPorts,
        List<ConnectionRule> connectionRules,
        Map<String, Size> sizeLimits,
        Map<String, List<StyleData>> allowedStyles
) {
    public record Size(int width, int height) {
    }

    Schema toSchema(String name) {
        Map<String, Dimension> sizes = new HashMap<>();
        orEmpty(sizeLimits).forEach((type, size) -> sizes.put(type, new Dimension(size.width(), size.height())));
        Map<String, List<Style>> styles = new HashMap<>();
        orEmpty(allowedStyles).forEach((type, list) -> styles.put(type, list.stream().map(StyleData::toStyle).toList()));
        return new Schema(name, allowedBlockTypes, allowedConnectorTypes, orEmpty(maxPorts),
                connectionRules == null ? List.of() : connectionRules, sizes, styles);
    }

    private static <V> Map<String, V> orEmpty(Map<String, V> map) {
        return map == null ? Map.of() : map;
    }
}
