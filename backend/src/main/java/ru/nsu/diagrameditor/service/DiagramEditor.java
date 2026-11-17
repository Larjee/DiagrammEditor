package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Service;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.repository.ResourceRepository;

@Service
public class DiagramEditor {

    private final ResourceRepository resources;
    private Diagram diagram;

    public DiagramEditor(ResourceRepository resources) {
        this.resources = resources;
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

    public Diagram getDiagram() {
        return diagram;
    }
}
