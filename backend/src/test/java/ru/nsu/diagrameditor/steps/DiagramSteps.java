package ru.nsu.diagrameditor.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.After;
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
import org.springframework.util.FileSystemUtils;
import ru.nsu.diagrameditor.api.BlockController.AddBlockRequest;
import ru.nsu.diagrameditor.api.ComponentController.MoveRequest;
import ru.nsu.diagrameditor.api.ComponentController.ResizeRequest;
import ru.nsu.diagrameditor.api.ConnectorController.AddConnectorRequest;
import ru.nsu.diagrameditor.api.DiagramController.CreateDiagramRequest;
import ru.nsu.diagrameditor.api.DiagramController.OpenDiagramRequest;
import ru.nsu.diagrameditor.api.DiagramController.SaveDiagramRequest;
import ru.nsu.diagrameditor.api.DiagramController.SelectComponentSetRequest;
import ru.nsu.diagrameditor.api.DiagramController.SelectSchemaRequest;
import ru.nsu.diagrameditor.api.Point;
import ru.nsu.diagrameditor.api.PortRef;
import ru.nsu.diagrameditor.service.DiagramEditor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class DiagramSteps {

    private static final Map<String, String> LINE_TYPES = Map.of(
            "сплошная", "SOLID",
            "пунктирная", "DASHED",
            "точечная", "DOTTED");

    private static final Map<String, String> SIDES = Map.of(
            "справа", "RIGHT",
            "слева", "LEFT",
            "сверху", "TOP",
            "снизу", "BOTTOM");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DiagramEditor editor;

    private final Map<String, String> ids = new HashMap<>();
    private MockHttpServletResponse response;
    private Path files;

    @Before
    public void closeDiagram() {
        ReflectionTestUtils.setField(editor, "diagram", null);
    }

    @Before
    public void createFilesDirectory() throws IOException {
        files = Files.createTempDirectory("diagram-editor");
    }

    @After
    public void deleteFilesDirectory() throws IOException {
        FileSystemUtils.deleteRecursively(files);
    }

    @Дано("файл {string} с содержимым:")
    public void fileHasContent(String name, String content) throws IOException {
        Files.writeString(file(name), content);
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

    @Дано("на диаграмме блок {string} под именем {string}")
    public void blockIsAdded(String blockType, String name) throws Exception {
        userAddsBlock(blockType, 0, 0);
        assertEquals(201, response.getStatus());
        ids.put(name, JsonPath.read(body(response), "$.id"));
    }

    @Дано("{string} и {string} соединены коннектором {string} под именем {string}")
    public void blocksAreConnected(String source, String target, String connectorType, String name) throws Exception {
        userConnects(source, target, connectorType);
        assertEquals(201, response.getStatus());
        ids.put(name, JsonPath.read(body(response), "$.id"));
    }

    @Дано("у коннектора {string} есть точка изгиба в {int}, {int}")
    public void connectorHasInflectionPoint(String name, int x, int y) throws Exception {
        userAddsInflectionPoint(name, x, y);
        assertEquals(201, response.getStatus());
    }

    @Дано("диаграмма сохранена в файл {string}")
    public void diagramIsSaved(String name) throws Exception {
        userSavesTo(name);
        assertEquals(200, response.getStatus());
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

    @Когда("пользователь соединяет {string} и {string} коннектором {string}")
    public void userConnects(String source, String target, String connectorType) throws Exception {
        send(post("/api/diagram/connectors"), new AddConnectorRequest(connectorType,
                new PortRef(id(source), "RIGHT", 0.5), new PortRef(id(target), "LEFT", 0.5)));
    }

    @Когда("пользователь тянет коннектор {string} от порта {string} блока {string} к порту {string} блока {string}")
    public void userDragsConnector(String connectorType, String sourceSide, String source,
                                   String targetSide, String target) throws Exception {
        send(post("/api/diagram/connectors"), new AddConnectorRequest(connectorType,
                new PortRef(id(source), SIDES.getOrDefault(sourceSide, sourceSide), 0.5),
                new PortRef(id(target), SIDES.getOrDefault(targetSide, targetSide), 0.5)));
    }

    @Когда("пользователь перемещает {string} в точку {int}, {int}")
    public void userMoves(String name, int x, int y) throws Exception {
        send(put("/api/diagram/components/{id}/position", id(name)), new MoveRequest(x, y));
    }

    @Когда("пользователь задаёт компоненту {string} размер {int} на {int}")
    public void userResizes(String name, int width, int height) throws Exception {
        send(put("/api/diagram/components/{id}/size", id(name)), new ResizeRequest(width, height));
    }

    @Когда("пользователь меняет у {string} цвет фона на {string}")
    public void userChangesBackground(String name, String color) throws Exception {
        changeStyle(name, "backgroundColor", color);
    }

    @Когда("пользователь меняет у {string} тип линии на {string}")
    public void userChangesLineType(String name, String lineType) throws Exception {
        changeStyle(name, "lineType", LINE_TYPES.getOrDefault(lineType, lineType));
    }

    @Когда("пользователь добавляет коннектору {string} точку изгиба в {int}, {int}")
    public void userAddsInflectionPoint(String name, int x, int y) throws Exception {
        send(post("/api/diagram/connectors/{id}/points", id(name)), new Point(x, y));
    }

    @Когда("пользователь переносит точку изгиба {int} коннектора {string} в {int}, {int}")
    public void userMovesInflectionPoint(int number, String name, int x, int y) throws Exception {
        send(put("/api/diagram/connectors/{id}/points/{index}", id(name), number - 1), new Point(x, y));
    }

    @Когда("пользователь открывает файл {string}")
    public void userOpensFile(String name) throws Exception {
        send(post("/api/diagram/open"), new OpenDiagramRequest(file(name).toString()));
    }

    @Когда("пользователь сохраняет диаграмму в файл {string}")
    public void userSavesTo(String name) throws Exception {
        send(post("/api/diagram/save"), new SaveDiagramRequest(file(name).toString(), false));
    }

    @Когда("пользователь сохраняет диаграмму в файл {string} с перезаписью")
    public void userOverwrites(String name) throws Exception {
        send(post("/api/diagram/save"), new SaveDiagramRequest(file(name).toString(), true));
    }

    @Когда("пользователь сохраняет диаграмму")
    public void userSaves() throws Exception {
        send(post("/api/diagram/save"), new SaveDiagramRequest(null, false));
    }

    @Когда("пользователь удаляет диаграмму")
    public void userDeletesDiagram() throws Exception {
        response = mvc.perform(delete("/api/diagram")).andReturn().getResponse();
    }

    @Когда("пользователь выбирает набор компонентов {string}")
    public void userSelectsComponentSet(String componentSet) throws Exception {
        send(put("/api/diagram/component-set"), new SelectComponentSetRequest(componentSet));
    }

    @Когда("пользователь выбирает схему {string}")
    public void userSelectsSchema(String schema) throws Exception {
        send(put("/api/diagram/schema"), new SelectSchemaRequest(schema));
    }

    @Когда("пользователь отключает схему")
    public void userRemovesSchema() throws Exception {
        userSelectsSchema(null);
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
        DocumentContext diagram = diagram();
        assertTrue(diagram.read("$.blocks", List.class).isEmpty());
        assertTrue(diagram.read("$.connectors", List.class).isEmpty());
    }

    @Тогда("на диаграмме нет коннекторов")
    public void diagramHasNoConnectors() throws Exception {
        assertTrue(diagram().read("$.connectors", List.class).isEmpty());
    }

    @Тогда("на диаграмме коннекторов: {int}")
    public void diagramHasConnectors(int count) throws Exception {
        assertEquals(count, diagram().read("$.connectors", List.class).size());
    }

    @Тогда("у блока {string} портов: {int}")
    public void blockHasPorts(String name, int count) throws Exception {
        assertEquals(count, ((List<?>) block(name).get("ports")).size());
    }

    @Тогда("на диаграмме коннектор {string} от {string} к {string}")
    public void diagramHasConnector(String connectorType, String source, String target) throws Exception {
        List<Object> found = diagram().read("$.connectors[?(@.type == '" + connectorType + "' && @.source.blockId == '"
                + id(source) + "' && @.target.blockId == '" + id(target) + "')]");
        assertEquals(1, found.size());
    }

    @Тогда("на диаграмме один блок {string} в точке {int}, {int}")
    public void diagramHasBlock(String blockType, int x, int y) throws Exception {
        DocumentContext diagram = diagram();
        assertEquals(1, (int) diagram.read("$.blocks.length()", Integer.class));
        assertEquals(blockType, diagram.read("$.blocks[0].type"));
        assertEquals(x, number(diagram, "$.blocks[0].x"));
        assertEquals(y, number(diagram, "$.blocks[0].y"));
    }

    @Тогда("блок {string} находится в точке {int}, {int}")
    public void blockIsAt(String name, int x, int y) throws Exception {
        Map<String, Object> block = block(name);
        assertEquals(x, ((Number) block.get("x")).doubleValue());
        assertEquals(y, ((Number) block.get("y")).doubleValue());
    }

    @Тогда("размер блока {string} равен {int} на {int}")
    public void blockHasSize(String name, int width, int height) throws Exception {
        Map<String, Object> block = block(name);
        assertEquals(width, ((Number) block.get("width")).doubleValue());
        assertEquals(height, ((Number) block.get("height")).doubleValue());
    }

    @Тогда("у {string} цвет фона {string}")
    public void componentHasBackground(String name, String color) throws Exception {
        assertEquals(color, style(name).get("backgroundColor"));
    }

    @Тогда("у {string} тип линии {string}")
    public void componentHasLineType(String name, String lineType) throws Exception {
        assertEquals(LINE_TYPES.getOrDefault(lineType, lineType), style(name).get("lineType"));
    }

    @Тогда("у коннектора {string} точек изгиба: {int}")
    public void connectorHasInflectionPoints(String name, int count) throws Exception {
        assertEquals(count, ((List<?>) connector(name).get("points")).size());
    }

    @Тогда("точка изгиба {int} коннектора {string} находится в {int}, {int}")
    public void inflectionPointIsAt(int number, String name, int x, int y) throws Exception {
        Map<?, ?> point = (Map<?, ?>) ((List<?>) connector(name).get("points")).get(number - 1);
        assertEquals(x, ((Number) point.get("x")).doubleValue());
        assertEquals(y, ((Number) point.get("y")).doubleValue());
    }

    @Тогда("открыта диаграмма из файла {string}")
    public void diagramIsOpenFromFile(String name) throws Exception {
        assertEquals(file(name).toString(), diagram().read("$.filePath"));
    }

    @Тогда("на диаграмме блоков: {int} и коннекторов: {int}")
    public void diagramHasComponents(int blocks, int connectors) throws Exception {
        DocumentContext diagram = diagram();
        assertEquals(blocks, diagram.read("$.blocks", List.class).size());
        assertEquals(connectors, diagram.read("$.connectors", List.class).size());
    }

    @Тогда("в файле {string} блоков: {int}")
    public void fileHasBlocks(String name, int count) throws Exception {
        assertEquals(count, JsonPath.parse(file(name).toFile()).read("$.blocks", List.class).size());
    }

    @Тогда("файла {string} нет")
    public void fileIsMissing(String name) {
        assertFalse(Files.exists(file(name)));
    }

    @Тогда("файл {string} содержит {string}")
    public void fileContains(String name, String text) throws IOException {
        assertTrue(Files.readString(file(name)).contains(text));
    }

    @Тогда("активен набор компонентов {string}")
    public void componentSetIsActive(String componentSet) throws Exception {
        assertEquals(componentSet, diagram().read("$.componentSet"));
    }

    @Тогда("активна схема {string}")
    public void schemaIsActive(String schema) throws Exception {
        assertEquals(schema, diagram().read("$.schema"));
    }

    @Тогда("в палитре есть блок {string}")
    public void paletteHasBlock(String blockType) throws Exception {
        assertTrue(diagram().read("$.blockTypes", List.class).contains(blockType));
    }

    @Тогда("в палитре нет блока {string}")
    public void paletteHasNoBlock(String blockType) throws Exception {
        assertFalse(diagram().read("$.blockTypes", List.class).contains(blockType));
    }

    @Когда("страница с адресом {string} спрашивает разрешение на {string} к {string}")
    public void pageAsksForAccess(String origin, String method, String path) throws Exception {
        response = mvc.perform(options(path)
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", method)
                        .header("Access-Control-Request-Headers", "content-type"))
                .andReturn().getResponse();
    }

    @Тогда("бэкенд разрешает запросы со страницы {string}")
    public void backendAllows(String origin) {
        assertEquals(200, response.getStatus());
        assertEquals(origin, response.getHeader("Access-Control-Allow-Origin"));
    }

    @Тогда("бэкенд запрещает запрос")
    public void backendForbids() {
        assertEquals(403, response.getStatus());
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

    private Path file(String name) {
        return files.resolve(name);
    }

    private String id(String name) {
        return ids.getOrDefault(name, name);
    }

    private void changeStyle(String name, String property, Object value) throws Exception {
        Map<String, Object> style = new HashMap<>(style(name));
        style.put(property, value);
        send(put("/api/diagram/components/{id}/style", id(name)), style);
    }

    private Map<String, Object> style(String name) throws Exception {
        List<Map<String, Object>> found = diagram().read("$..[?(@.id == '" + id(name) + "')].style");
        assertEquals(1, found.size());
        return found.get(0);
    }

    private Map<String, Object> block(String name) throws Exception {
        List<Map<String, Object>> found = diagram().read("$.blocks[?(@.id == '" + id(name) + "')]");
        assertEquals(1, found.size());
        return found.get(0);
    }

    private Map<String, Object> connector(String name) throws Exception {
        List<Map<String, Object>> found = diagram().read("$.connectors[?(@.id == '" + id(name) + "')]");
        assertEquals(1, found.size());
        return found.get(0);
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
