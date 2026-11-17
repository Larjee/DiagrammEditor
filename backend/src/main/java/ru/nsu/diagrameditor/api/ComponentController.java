package ru.nsu.diagrameditor.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.service.DiagramEditor;

@RestController
@RequestMapping("/api/diagram/components/{id}")
public class ComponentController {

    private final DiagramEditor editor;

    public ComponentController(DiagramEditor editor) {
        this.editor = editor;
    }

    @PutMapping("/size")
    public BlockResponse resize(@PathVariable String id, @RequestBody ResizeRequest request) {
        Block block = editor.resizeComponent(id, request.width(), request.height());
        return BlockResponse.from(block);
    }

    public record ResizeRequest(double width, double height) {
    }
}
