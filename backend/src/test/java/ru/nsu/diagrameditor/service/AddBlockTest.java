package ru.nsu.diagrameditor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.repository.InMemoryResourceRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddBlockTest {

    private DiagramEditor editor;

    @BeforeEach
    void setUp() {
        editor = new DiagramEditor(new InMemoryResourceRepository(), new SchemaValidator());
    }

    @Test
    void addsBlockPermittedBySchema() {
        editor.createDiagram("uml-class", "uml-class");

        Block block = editor.addBlock("class", 10, 20);

        assertEquals("class", block.getType());
        assertEquals(10.0, block.getX());
        assertEquals(20.0, block.getY());
        assertNotNull(block.getStyle());
        assertEquals(List.of(block), editor.getDiagram().getComponents());
    }

    @Test
    void rejectsBlockNotPermittedBySchemaInStrictMode() {
        editor.createDiagram("uml-class", "uml-class");

        assertThrows(SchemaViolationException.class, () -> editor.addBlock("decision", 0, 0));

        assertTrue(editor.getDiagram().getComponents().isEmpty());
    }

    @Test
    void addsAnyBlockInFreeMode() {
        editor.createDiagram("uml-class", null);

        Block block = editor.addBlock("decision", 0, 0);

        assertEquals(List.of(block), editor.getDiagram().getComponents());
    }

    @Test
    void failsWhenNoDiagramIsOpen() {
        assertThrows(NoOpenDiagramException.class, () -> editor.addBlock("class", 0, 0));
    }
}
