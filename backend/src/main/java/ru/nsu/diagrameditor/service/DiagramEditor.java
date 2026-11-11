package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Service;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.EditMode;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.repository.ResourceRepository;

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
        Diagram diagram = requireDiagram();
        if (diagram.getMode() == EditMode.STRICT && !validator.canAddBlock(diagram, blockType)) {
            throw new SchemaViolationException(
                    "Block '" + blockType + "' is not permitted by schema '" + diagram.getSchema().getName() + "'");
        }
        Block block = new Block(blockType, x, y, diagram.getComponentSet().getDefaultStyle(blockType));
        diagram.addComponent(block);
        return block;
    }

    public Diagram getDiagram() {
        return diagram;
    }

    private Diagram requireDiagram() {
        if (diagram == null) {
            throw new IllegalStateException("No diagram is open for editing");
        }
        return diagram;
    }
}
