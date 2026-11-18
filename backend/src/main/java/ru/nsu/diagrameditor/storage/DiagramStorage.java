package ru.nsu.diagrameditor.storage;

import ru.nsu.diagrameditor.model.Diagram;

public interface DiagramStorage {

    Diagram load(String path);

    boolean exists(String path);

    void save(Diagram diagram, String path);

    void delete(String path);
}
