package ru.nsu.diagrameditor.model;

import java.awt.Color;
import java.awt.Font;

public record Style(
        Color borderColor,
        double borderWidth,
        Color backgroundColor,
        Font font,
        LineType lineType
) {
}
