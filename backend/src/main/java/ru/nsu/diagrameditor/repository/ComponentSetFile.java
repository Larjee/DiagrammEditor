package ru.nsu.diagrameditor.repository;

import ru.nsu.diagrameditor.model.ComponentSet;

import java.util.List;

public record ComponentSetFile(List<String> blockTypes, List<String> connectorTypes) {

    ComponentSet toComponentSet(String name) {
        return new ComponentSet(name, blockTypes, connectorTypes);
    }
}
