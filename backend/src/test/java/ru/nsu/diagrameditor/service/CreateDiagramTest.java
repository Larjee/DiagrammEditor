package ru.nsu.diagrameditor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nsu.diagrameditor.TestEditors;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.EditMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateDiagramTest {

    private DiagramEditor editor;

    @BeforeEach
    void setUp() {
        editor = TestEditors.create();
    }

    @Test
    void createsStrictDiagramWhenSchemaIsSelected() {
        Diagram diagram = editor.createDiagram("uml-class", "uml-class");

        assertEquals(EditMode.STRICT, diagram.getMode());
        assertEquals("uml-class", diagram.getComponentSet().getName());
        assertEquals("uml-class", diagram.getSchema().getName());
        assertTrue(diagram.getComponents().isEmpty());
        assertSame(diagram, editor.getDiagram());
    }

    @Test
    void createsFreeDiagramWithoutSchema() {
        Diagram diagram = editor.createDiagram("flowchart", null);

        assertEquals(EditMode.FREE, diagram.getMode());
        assertNull(diagram.getSchema());
    }

    @Test
    void rejectsUnavailableSchema() {
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> editor.createDiagram("uml-class", "missing"));

        assertTrue(e.getMessage().contains("continue without one"));
        assertNull(editor.getDiagram());
    }

    @Test
    void keepsOpenDiagramWhenSchemaIsUnavailable() {
        Diagram opened = editor.createDiagram("uml-class", null);

        assertThrows(ResourceNotFoundException.class, () -> editor.createDiagram("uml-class", "missing"));

        assertSame(opened, editor.getDiagram());
    }

    @Test
    void rejectsUnknownComponentSet() {
        assertThrows(ResourceNotFoundException.class, () -> editor.createDiagram("missing", null));
    }
}
