package ru.nsu.diagrameditor.steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import ru.nsu.diagrameditor.TestEditors;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.Component;
import ru.nsu.diagrameditor.model.EditMode;
import ru.nsu.diagrameditor.service.DiagramEditor;
import ru.nsu.diagrameditor.service.EditorException;
import ru.nsu.diagrameditor.service.NoOpenDiagramException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiagramSteps {

    private final DiagramEditor editor = TestEditors.create();
    private EditorException error;

    @Дано("открыта диаграмма с набором {string} и схемой {string}")
    public void diagramIsOpen(String componentSet, String schema) {
        editor.createDiagram(componentSet, schema);
    }

    @Дано("открыта диаграмма с набором {string} без схемы")
    public void freeDiagramIsOpen(String componentSet) {
        editor.createDiagram(componentSet, null);
    }

    @Когда("пользователь создаёт диаграмму с набором {string} и схемой {string}")
    public void userCreatesDiagram(String componentSet, String schema) {
        attempt(() -> editor.createDiagram(componentSet, schema));
    }

    @Когда("пользователь создаёт диаграмму с набором {string} без схемы")
    public void userCreatesFreeDiagram(String componentSet) {
        attempt(() -> editor.createDiagram(componentSet, null));
    }

    @Когда("пользователь добавляет блок {string} в точку {int}, {int}")
    public void userAddsBlock(String blockType, int x, int y) {
        attempt(() -> editor.addBlock(blockType, x, y));
    }

    @Тогда("диаграмма открыта в строгом режиме")
    public void diagramIsStrict() {
        assertEquals(EditMode.STRICT, editor.getDiagram().getMode());
    }

    @Тогда("диаграмма открыта в свободном режиме")
    public void diagramIsFree() {
        assertEquals(EditMode.FREE, editor.getDiagram().getMode());
    }

    @Тогда("диаграмма не открыта")
    public void noDiagramIsOpen() {
        assertThrows(NoOpenDiagramException.class, editor::getDiagram);
    }

    @Тогда("на диаграмме нет компонентов")
    public void diagramIsEmpty() {
        assertTrue(editor.getDiagram().getComponents().isEmpty());
    }

    @Тогда("на диаграмме один блок {string} в точке {int}, {int}")
    public void diagramHasBlock(String blockType, int x, int y) {
        List<Component> components = editor.getDiagram().getComponents();
        assertEquals(1, components.size());
        Block block = (Block) components.get(0);
        assertEquals(blockType, block.getType());
        assertEquals(x, block.getX());
        assertEquals(y, block.getY());
    }

    @Тогда("редактор отказывает с ошибкой {int}")
    public void editorRejects(int status) {
        assertNotNull(error);
        assertEquals(status, error.getStatus().value());
    }

    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (EditorException e) {
            error = e;
        }
    }
}
