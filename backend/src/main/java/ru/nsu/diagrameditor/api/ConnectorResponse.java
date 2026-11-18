package ru.nsu.diagrameditor.api;

import ru.nsu.diagrameditor.model.Connector;

import java.util.List;

public record ConnectorResponse(
        String id,
        String type,
        PortRef source,
        PortRef target,
        List<Point> points,
        StyleResponse style
) {
    static ConnectorResponse from(Connector connector) {
        return new ConnectorResponse(connector.getId(), connector.getType(),
                PortRef.from(connector.getSource()), PortRef.from(connector.getTarget()),
                connector.getInflectionPoints().stream().map(Point::from).toList(),
                StyleResponse.from(connector.getStyle()));
    }
}
