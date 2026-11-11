package ru.nsu.diagrameditor.model;

public record ConnectionRule(
        String connectorType,
        String sourceBlockType,
        String targetBlockType
) {
}
