package ru.nsu.diagrameditor.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.Before;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.nsu.diagrameditor.api.BlockController.AddBlockRequest;
import ru.nsu.diagrameditor.api.DiagramController.CreateDiagramRequest;
import ru.nsu.diagrameditor.service.DiagramEditor;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class DiagramSteps {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DiagramEditor editor;

    private MockHttpServletResponse response;

    @Before
    public void closeDiagram() {
        ReflectionTestUtils.setField(editor, "diagram", null);
    }

    @Дано("открыта диаграмма с набором {string} и схемой {string}")
    public void diagramIsOpen(String componentSet, String schema) throws Exception {
        userCreatesDiagram(componentSet, schema);
        assertEquals(201, response.getStatus());
    }

    @Дано("открыта диаграмма с набором {string} без схемы")
    public void freeDiagramIsOpen(String componentSet) throws Exception {
        diagramIsOpen(componentSet, null);
    }

    @Когда("пользователь создаёт диаграмму с набором {string} и схемой {string}")
    public void userCreatesDiagram(String componentSet, String schema) throws Exception {
        send(post("/api/diagram"), new CreateDiagramRequest(componentSet, schema));
    }

    @Когда("пользователь создаёт диаграмму с набором {string} без схемы")
    public void userCreatesFreeDiagram(String componentSet) throws Exception {
        userCreatesDiagram(componentSet, null);
    }

    @Когда("пользователь добавляет блок {string} в точку {int}, {int}")
    public void userAddsBlock(String blockType, int x, int y) throws Exception {
        send(post("/api/diagram/blocks"), new AddBlockRequest(blockType, x, y));
    }

    @Тогда("диаграмма открыта в строгом режиме")
    public void diagramIsStrict() throws Exception {
        assertEquals("STRICT", diagram().read("$.mode"));
    }

    @Тогда("диаграмма открыта в свободном режиме")
    public void diagramIsFree() throws Exception {
        assertEquals("FREE", diagram().read("$.mode"));
    }

    @Тогда("диаграмма не открыта")
    public void noDiagramIsOpen() throws Exception {
        assertEquals(409, mvc.perform(get("/api/diagram")).andReturn().getResponse().getStatus());
    }

    @Тогда("на диаграмме нет компонентов")
    public void diagramIsEmpty() throws Exception {
        List<Object> blocks = diagram().read("$.blocks");
        assertTrue(blocks.isEmpty());
    }

    @Тогда("на диаграмме один блок {string} в точке {int}, {int}")
    public void diagramHasBlock(String blockType, int x, int y) throws Exception {
        DocumentContext diagram = diagram();
        assertEquals(1, (int) diagram.read("$.blocks.length()", Integer.class));
        assertEquals(blockType, diagram.read("$.blocks[0].type"));
        assertEquals(x, number(diagram, "$.blocks[0].x"));
        assertEquals(y, number(diagram, "$.blocks[0].y"));
    }

    @Тогда("редактор отказывает с ошибкой {int}")
    public void editorRejects(int status) throws Exception {
        assertEquals(status, response.getStatus());
        assertNotNull(JsonPath.read(body(response), "$.message"));
    }

    private void send(MockHttpServletRequestBuilder request, Object body) throws Exception {
        response = mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body)))
                .andReturn().getResponse();
    }

    private DocumentContext diagram() throws Exception {
        MockHttpServletResponse diagram = mvc.perform(get("/api/diagram")).andReturn().getResponse();
        assertEquals(200, diagram.getStatus());
        return JsonPath.parse(body(diagram));
    }

    private static double number(DocumentContext json, String path) {
        return json.read(path, Double.class);
    }

    private static String body(MockHttpServletResponse response) throws Exception {
        return response.getContentAsString(StandardCharsets.UTF_8);
    }
}
