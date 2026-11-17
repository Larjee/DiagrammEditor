package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Service;
import ru.nsu.diagrameditor.model.Component;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.Port;
import ru.nsu.diagrameditor.model.Style;

@Service
public class SchemaValidator {

    public boolean canAddBlock(Diagram diagram, String blockType) {
        return diagram.getSchema().isBlockAllowed(blockType);
    }

    public boolean canConnect(Diagram diagram, String connectorType, Port sourcePort, Port targetPort) {
        return diagram.getSchema().isConnectionAllowed(
                connectorType, sourcePort.getOwner().getType(), targetPort.getOwner().getType());
    }

    public boolean canResize(Diagram diagram, Component component, double width, double height) {
        return diagram.getSchema().isSizeAllowed(component.getType(), width, height);
    }

    public boolean canApplyStyle(Diagram diagram, Component component, Style style) {
        return diagram.getSchema().isStyleAllowed(component.getType(), style);
    }
}
