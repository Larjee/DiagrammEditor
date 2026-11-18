package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Service;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.Component;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Connector;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.EditMode;
import ru.nsu.diagrameditor.model.Port;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.model.Style;
import ru.nsu.diagrameditor.repository.ResourceRepository;

import java.util.List;

@Service
public class DiagramEditor {

    private final ResourceRepository resources;
    private final SchemaValidator validator;
    private Diagram diagram;

    public DiagramEditor(ResourceRepository resources, SchemaValidator validator) {
        this.resources = resources;
        this.validator = validator;
    }

    public Diagram createDiagram(String componentSetName, String schemaName) {
        ComponentSet componentSet = resources.loadComponentSet(componentSetName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Component set '" + componentSetName + "' is not available"));
        Schema schema = null;
        if (schemaName != null) {
            schema = resources.loadSchema(schemaName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Schema '" + schemaName + "' is invalid or unavailable. "
                                    + "Select another schema or continue without one"));
        }
        diagram = new Diagram(componentSet, schema);
        return diagram;
    }

    public Block addBlock(String blockType, double x, double y) {
        Diagram diagram = getDiagram();
        if (diagram.getMode() == EditMode.STRICT && !validator.canAddBlock(diagram, blockType)) {
            throw new SchemaViolationException(
                    "Block '" + blockType + "' is not permitted by schema '" + diagram.getSchema().getName() + "'");
        }
        Block block = new Block(blockType, x, y, diagram.getComponentSet().getDefaultStyle(blockType));
        diagram.addComponent(block);
        return block;
    }

    public Port findPort(String blockId, String side, double offset) {
        if (side == null) {
            throw new InvalidRequestException("Port side is required");
        }
        Block block = getBlock(blockId);
        return block.getPorts().stream()
                .filter(port -> port.getSide().equals(side) && port.getOffset() == offset)
                .findFirst()
                .orElseGet(() -> new Port(block, side, offset));
    }

    public Connector addConnector(String connectorType, Port sourcePort, Port targetPort) {
        Diagram diagram = getDiagram();
        if (diagram.getMode() == EditMode.STRICT
                && !validator.canConnect(diagram, connectorType, sourcePort, targetPort)) {
            throw new SchemaViolationException("Connector '" + connectorType + "' from '"
                    + sourcePort.getOwner().getType() + "' to '" + targetPort.getOwner().getType()
                    + "' is not permitted by schema '" + diagram.getSchema().getName() + "'");
        }
        Connector connector = new Connector(connectorType, diagram.getComponentSet().getDefaultStyle(connectorType));
        addPortIfNew(sourcePort);
        addPortIfNew(targetPort);
        connector.connect(sourcePort, targetPort);
        diagram.addComponent(connector);
        return connector;
    }

    public Block resizeComponent(String componentId, double width, double height) {
        Diagram diagram = getDiagram();
        if (!(getComponent(componentId) instanceof Block block)) {
            throw new InvalidRequestException("Connector size is defined by its ports");
        }
        if (width <= 0 || height <= 0) {
            throw new InvalidRequestException("Size must be positive");
        }
        if (diagram.getMode() == EditMode.STRICT && !validator.canResize(diagram, block, width, height)) {
            throw new SchemaViolationException("Size " + width + " x " + height + " of '" + block.getType()
                    + "' is beyond the limits of schema '" + diagram.getSchema().getName() + "'");
        }
        block.resize(width, height);
        return block;
    }

    public Component changeStyle(String componentId, Style style) {
        Diagram diagram = getDiagram();
        Component component = getComponent(componentId);
        if (diagram.getMode() == EditMode.STRICT && !validator.canApplyStyle(diagram, component, style)) {
            throw new SchemaViolationException("Style is not available for '" + component.getType()
                    + "' in schema '" + diagram.getSchema().getName() + "'");
        }
        component.setStyle(style);
        return component;
    }

    public Diagram getDiagram() {
        if (diagram == null) {
            throw new NoOpenDiagramException();
        }
        return diagram;
    }

    private Component getComponent(String id) {
        return getDiagram().getComponents().stream()
                .filter(component -> component.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Component '" + id + "' not found"));
    }

    private Block getBlock(String id) {
        if (getComponent(id) instanceof Block block) {
            return block;
        }
        throw new InvalidRequestException("Component '" + id + "' is not a block");
    }

    private static void addPortIfNew(Port port) {
        List<Port> ports = port.getOwner().getPorts();
        if (!ports.contains(port)) {
            ports.add(port);
        }
    }
}
