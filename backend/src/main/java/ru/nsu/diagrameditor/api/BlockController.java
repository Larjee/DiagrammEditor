package ru.nsu.diagrameditor.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.LineType;
import ru.nsu.diagrameditor.model.Style;
import ru.nsu.diagrameditor.service.DiagramEditor;

import java.awt.Color;

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

    public record BlockResponse(
            String id,
            String type,
            double x,
            double y,
            double width,
            double height,
            StyleResponse style
    ) {
        static BlockResponse from(Block block) {
            return new BlockResponse(block.getId(), block.getType(), block.getX(), block.getY(),
                    block.getWidth(), block.getHeight(), StyleResponse.from(block.getStyle()));
        }
    }

    public record StyleResponse(
            String borderColor,
            double borderWidth,
            String backgroundColor,
            String font,
            int fontSize,
            LineType lineType
    ) {
        static StyleResponse from(Style style) {
            return new StyleResponse(hex(style.borderColor()), style.borderWidth(), hex(style.backgroundColor()),
                    style.font().getName(), style.font().getSize(), style.lineType());
        }

        private static String hex(Color color) {
            return String.format("#%06x", color.getRGB() & 0xffffff);
        }
    }
}
