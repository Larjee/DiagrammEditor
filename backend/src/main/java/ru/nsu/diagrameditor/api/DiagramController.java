package ru.nsu.diagrameditor.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.EditMode;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.repository.ResourceRepository;
import ru.nsu.diagrameditor.service.DiagramEditor;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DiagramController {

    private final DiagramEditor editor;
    private final ResourceRepository resources;

    public DiagramController(DiagramEditor editor, ResourceRepository resources) {
        this.editor = editor;
        this.resources = resources;
    }

    @GetMapping("/component-sets")
    public List<String> getComponentSets() {
        return resources.getComponentSets().stream().map(ComponentSet::getName).toList();
    }

    @GetMapping("/schemas")
    public List<String> getSchemas() {
        return resources.getSchemas().stream().map(Schema::getName).toList();
    }

    @PostMapping("/diagram")
    @ResponseStatus(HttpStatus.CREATED)
    public DiagramResponse createDiagram(@RequestBody CreateDiagramRequest request) {
        Diagram diagram = editor.createDiagram(request.componentSet(), request.schema());
        return DiagramResponse.from(diagram);
    }

    public record CreateDiagramRequest(String componentSet, String schema) {
    }

    public record DiagramResponse(
            String componentSet,
            String schema,
            EditMode mode,
            List<String> blockTypes,
            List<String> connectorTypes
    ) {
        static DiagramResponse from(Diagram diagram) {
            ComponentSet set = diagram.getComponentSet();
            String schema = diagram.getSchema() == null ? null : diagram.getSchema().getName();
            return new DiagramResponse(set.getName(), schema, diagram.getMode(),
                    set.getBlockTypes(), set.getConnectorTypes());
        }
    }
}
