package ru.nsu.diagrameditor.api;

import ru.nsu.diagrameditor.model.Connector;

public record ConnectorResponse(
        String id,
        String type,
        PortRef source,
        PortRef target,
        StyleResponse style
) {
    static ConnectorResponse from(Connector connector) {
        return new ConnectorResponse(connector.getId(), connector.getType(),
                PortRef.from(connector.getSource()), PortRef.from(connector.getTarget()),
                StyleResponse.from(connector.getStyle()));
    }
}
