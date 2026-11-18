package ru.nsu.diagrameditor.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.Component;
import ru.nsu.diagrameditor.model.LineType;
import ru.nsu.diagrameditor.model.Style;
import ru.nsu.diagrameditor.service.DiagramEditor;
import ru.nsu.diagrameditor.service.InvalidRequestException;

import java.awt.Color;
import java.awt.Font;

@RestController
@RequestMapping("/api/diagram/components/{id}")
public class ComponentController {

    private final DiagramEditor editor;

    public ComponentController(DiagramEditor editor) {
        this.editor = editor;
    }

    @PutMapping("/position")
    public BlockResponse move(@PathVariable String id, @RequestBody MoveRequest request) {
        Block block = editor.moveComponent(id, request.x(), request.y());
        return BlockResponse.from(block);
    }

    @PutMapping("/size")
    public BlockResponse resize(@PathVariable String id, @RequestBody ResizeRequest request) {
        Block block = editor.resizeComponent(id, request.width(), request.height());
        return BlockResponse.from(block);
    }

    @PutMapping("/style")
    public StyleResponse changeStyle(@PathVariable String id, @RequestBody StyleRequest request) {
        Component component = editor.changeStyle(id, request.toStyle());
        return StyleResponse.from(component.getStyle());
    }

    public record MoveRequest(double x, double y) {
    }

    public record ResizeRequest(double width, double height) {
    }

    public record StyleRequest(
            String borderColor,
            double borderWidth,
            String backgroundColor,
            String font,
            int fontSize,
            LineType lineType
    ) {
        Style toStyle() {
            if (borderColor == null || backgroundColor == null || font == null || lineType == null) {
                throw new InvalidRequestException("Style needs border color, background color, font and line type");
            }
            return new Style(color(borderColor), borderWidth, color(backgroundColor),
                    new Font(font, Font.PLAIN, fontSize), lineType);
        }

        private static Color color(String value) {
            try {
                return Color.decode(value);
            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Invalid color '" + value + "'");
            }
        }
    }
}
