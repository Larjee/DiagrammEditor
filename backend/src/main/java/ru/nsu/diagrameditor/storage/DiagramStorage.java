package ru.nsu.diagrameditor.storage;

import ru.nsu.diagrameditor.model.Diagram;

public interface DiagramStorage {

    Diagram load(String path);
}
