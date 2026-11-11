package ru.nsu.diagrameditor.repository;

import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Schema;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository {

    List<ComponentSet> getComponentSets();

    List<Schema> getSchemas();

    Optional<ComponentSet> loadComponentSet(String name);

    Optional<Schema> loadSchema(String name);
}
