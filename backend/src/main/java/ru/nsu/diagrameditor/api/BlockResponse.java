package ru.nsu.diagrameditor.api;

import ru.nsu.diagrameditor.model.Block;

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
