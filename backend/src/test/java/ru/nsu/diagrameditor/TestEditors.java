package ru.nsu.diagrameditor;

import ru.nsu.diagrameditor.repository.InMemoryResourceRepository;
import ru.nsu.diagrameditor.service.DiagramEditor;
import ru.nsu.diagrameditor.service.SchemaValidator;

public final class TestEditors {

    private TestEditors() {
    }

    public static DiagramEditor create() {
        return new DiagramEditor(new InMemoryResourceRepository(), new SchemaValidator());
    }
}
