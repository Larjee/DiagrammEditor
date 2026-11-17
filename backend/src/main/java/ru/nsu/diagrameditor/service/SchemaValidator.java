package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Component;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.Port;

@Component
public class SchemaValidator {

    public boolean canAddBlock(Diagram diagram, String blockType) {
        return diagram.getSchema().isBlockAllowed(blockType);
    }

    public boolean canConnect(Diagram diagram, String connectorType, Port sourcePort, Port targetPort) {
        return diagram.getSchema().isConnectionAllowed(
                connectorType, sourcePort.getOwner().getType(), targetPort.getOwner().getType());
    }
}
