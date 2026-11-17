package ru.nsu.diagrameditor.api;

import ru.nsu.diagrameditor.model.LineType;
import ru.nsu.diagrameditor.model.Style;

import java.awt.Color;

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
