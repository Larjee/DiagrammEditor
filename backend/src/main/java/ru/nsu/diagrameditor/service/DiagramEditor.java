package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Service;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.Component;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Connector;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.EditMode;
import ru.nsu.diagrameditor.model.InflectionPoint;
import ru.nsu.diagrameditor.model.Port;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.model.Style;
import ru.nsu.diagrameditor.repository.ResourceRepository;
import ru.nsu.diagrameditor.storage.DiagramStorage;

import java.util.List;

@Service
public class DiagramEditor {

    private final ResourceRepository resources;
    private final SchemaValidator validator;
    private final DiagramStorage storage;
    private Diagram diagram;

    public DiagramEditor(ResourceRepository resources, SchemaValidator validator, DiagramStorage storage) {
        this.resources = resources;
        this.validator = validator;
        this.storage = storage;
    }

    public Diagram createDiagram(String componentSetName, String schemaName) {
        ComponentSet componentSet = loadComponentSet(componentSetName);
        Schema schema = schemaName == null ? null : loadSchema(schemaName);
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

    public Diagram openDiagram(String path) {
        if (path == null) {
            throw new InvalidRequestException("Choose a file to open");
        }
        Diagram opened = storage.load(path);
        opened.setFilePath(path);
        diagram = opened;
        return diagram;
    }

    public Diagram setComponentSet(String componentSetName) {
        Diagram diagram = getDiagram();
        diagram.setComponentSet(loadComponentSet(componentSetName));
        return diagram;
    }

    public Diagram setSchema(String schemaName) {
        Diagram diagram = getDiagram();
        diagram.setSchema(schemaName == null ? null : loadSchema(schemaName));
        return diagram;
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
                    + "' violates schema '" + diagram.getSchema().getName() + "'");
        }
        Connector connector = new Connector(connectorType, diagram.getComponentSet().getDefaultStyle(connectorType));
        addPortIfNew(sourcePort);
        addPortIfNew(targetPort);
        connector.connect(sourcePort, targetPort);
        diagram.addComponent(connector);
        return connector;
    }

    public Connector addInflectionPoint(String connectorId, double x, double y) {
        Connector connector = getConnector(connectorId);
        connector.getInflectionPoints().add(new InflectionPoint(x, y));
        return connector;
    }

    public Connector moveInflectionPoint(String connectorId, int index, double x, double y) {
        Connector connector = getConnector(connectorId);
        List<InflectionPoint> points = connector.getInflectionPoints();
        if (index < 0 || index >= points.size()) {
            throw new ResourceNotFoundException("Inflection point " + index + " not found");
        }
        connector.moveInflectionPoint(points.get(index), x, y);
        return connector;
    }

    public Block moveComponent(String componentId, double x, double y) {
        if (!(getComponent(componentId) instanceof Block block)) {
            throw new InvalidRequestException("Connector position is defined by its ports");
        }
        block.moveTo(x, y);
        return block;
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

    private ComponentSet loadComponentSet(String name) {
        return resources.loadComponentSet(name)
                .orElseThrow(() -> new ResourceNotFoundException("Component set '" + name + "' is not available"));
    }

    private Schema loadSchema(String name) {
        return resources.loadSchema(name)
                .orElseThrow(() -> new ResourceNotFoundException("Schema '" + name + "' is invalid or unavailable. "
                        + "Select another schema or continue without one"));
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

    private Connector getConnector(String id) {
        if (getComponent(id) instanceof Connector connector) {
            return connector;
        }
        throw new InvalidRequestException("Component '" + id + "' is not a connector");
    }

    private static void addPortIfNew(Port port) {
        List<Port> ports = port.getOwner().getPorts();
        if (!ports.contains(port)) {
            ports.add(port);
        }
    }
}
