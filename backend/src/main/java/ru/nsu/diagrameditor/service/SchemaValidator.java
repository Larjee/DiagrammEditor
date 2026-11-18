package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Service;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.Component;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.Port;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.model.Style;

@Service
public class SchemaValidator {

    public boolean canAddBlock(Diagram diagram, String blockType) {
        return diagram.getSchema().isBlockAllowed(blockType);
    }

    public boolean canConnect(Diagram diagram, String connectorType, Port sourcePort, Port targetPort) {
        Schema schema = diagram.getSchema();
        return schema.isConnectionAllowed(connectorType, sourcePort.getOwner().getType(), targetPort.getOwner().getType())
                && fitsPortLimit(schema, sourcePort)
                && fitsPortLimit(schema, targetPort);
    }

    public boolean canResize(Diagram diagram, Component component, double width, double height) {
        return diagram.getSchema().isSizeAllowed(component.getType(), width, height);
    }

    public boolean canApplyStyle(Diagram diagram, Component component, Style style) {
        return diagram.getSchema().isStyleAllowed(component.getType(), style);
    }

    private static boolean fitsPortLimit(Schema schema, Port port) {
        Block owner = port.getOwner();
        return owner.getPorts().contains(port) || owner.getPorts().size() < schema.getMaxPorts(owner.getType());
    }
}
