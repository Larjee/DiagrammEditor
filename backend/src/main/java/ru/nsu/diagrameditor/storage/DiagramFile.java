package ru.nsu.diagrameditor.storage;

import ru.nsu.diagrameditor.model.LineType;
import ru.nsu.diagrameditor.model.Style;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

public record DiagramFile(
        int formatVersion,
        String name,
        String componentSet,
        String schema,
        List<BlockData> blocks,
        List<ConnectorData> connectors
) {
    public static final int FORMAT_VERSION = 1;

    public record BlockData(
            String id,
            String type,
            double x,
            double y,
            double width,
            double height,
            String label,
            List<PortData> ports,
            StyleData style
    ) {
    }

    public record PortData(String side, double offset) {
    }

    public record ConnectorData(
            String type,
            EndData source,
            EndData target,
            List<PointData> points,
            StyleData style
    ) {
    }

    public record EndData(String blockId, String side, double offset) {
    }

    public record PointData(double x, double y) {
    }

    public record StyleData(
            String borderColor,
            double borderWidth,
            String backgroundColor,
            String font,
            int fontSize,
            LineType lineType
    ) {
        Style toStyle() {
            return new Style(Color.decode(borderColor), borderWidth, Color.decode(backgroundColor),
                    new Font(font, Font.PLAIN, fontSize), lineType);
        }
    }
}
