package ru.nsu.diagrameditor.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.service.DiagramEditor;

@RestController
@RequestMapping("/api/diagram/blocks")
public class BlockController {

    private final DiagramEditor editor;

    public BlockController(DiagramEditor editor) {
        this.editor = editor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlockResponse addBlock(@RequestBody AddBlockRequest request) {
        Block block = editor.addBlock(request.blockType(), request.x(), request.y());
        return BlockResponse.from(block);
    }

    public record AddBlockRequest(String blockType, double x, double y) {
    }
}
