package ru.nsu.diagrameditor.api;

import ru.nsu.diagrameditor.model.Block;

import java.util.List;

public record BlockResponse(
        String id,
        String type,
        double x,
        double y,
        double width,
        double height,
        List<PortRef> ports,
        StyleResponse style
) {
    static BlockResponse from(Block block) {
        return new BlockResponse(block.getId(), block.getType(), block.getX(), block.getY(),
                block.getWidth(), block.getHeight(), block.getPorts().stream().map(PortRef::from).toList(),
                StyleResponse.from(block.getStyle()));
    }
}
