package ru.nsu.diagrameditor.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import ru.nsu.diagrameditor.model.Block;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Connector;
import ru.nsu.diagrameditor.model.Diagram;
import ru.nsu.diagrameditor.model.InflectionPoint;
import ru.nsu.diagrameditor.model.Port;
import ru.nsu.diagrameditor.model.Schema;
import ru.nsu.diagrameditor.repository.ResourceRepository;
import ru.nsu.diagrameditor.service.DiagramFileException;
import ru.nsu.diagrameditor.service.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JsonDiagramStorage implements DiagramStorage {

    private final ObjectMapper mapper;
    private final ResourceRepository resources;

    public JsonDiagramStorage(ObjectMapper mapper, ResourceRepository resources) {
        this.mapper = mapper;
        this.resources = resources;
    }

    @Override
    public Diagram load(String path) {
        Path file = Path.of(path);
        if (!Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("File '" + path + "' not found");
        }
        DiagramFile data;
        try {
            data = mapper.readValue(file.toFile(), DiagramFile.class);
        } catch (JsonProcessingException e) {
            throw invalidFormat(path);
        } catch (IOException e) {
            throw new DiagramFileException("File '" + path + "' cannot be opened");
        }
        try {
            return toDiagram(data);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw invalidFormat(path);
        }
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(Path.of(path));
    }

    @Override
    public void save(Diagram diagram, String path) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(Path.of(path).toFile(), toFile(diagram));
        } catch (IOException e) {
            throw new DiagramFileException("Cannot write to '" + path + "'");
        }
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            throw new DiagramFileException("Cannot delete '" + path + "'");
        }
    }

    private static DiagramFile toFile(Diagram diagram) {
        List<DiagramFile.BlockData> blocks = diagram.getComponents().stream()
                .filter(Block.class::isInstance)
                .map(Block.class::cast)
                .map(block -> new DiagramFile.BlockData(block.getId(), block.getType(), block.getX(), block.getY(),
                        block.getWidth(), block.getHeight(), block.getLabel(),
                        block.getPorts().stream()
                                .map(port -> new DiagramFile.PortData(port.getSide(), port.getOffset()))
                                .toList(),
                        DiagramFile.StyleData.from(block.getStyle())))
                .toList();
        List<DiagramFile.ConnectorData> connectors = diagram.getComponents().stream()
                .filter(Connector.class::isInstance)
                .map(Connector.class::cast)
                .map(connector -> new DiagramFile.ConnectorData(connector.getType(),
                        end(connector.getSource()), end(connector.getTarget()),
                        connector.getInflectionPoints().stream()
                                .map(point -> new DiagramFile.PointData(point.getX(), point.getY()))
                                .toList(),
                        DiagramFile.StyleData.from(connector.getStyle())))
                .toList();
        String schema = diagram.getSchema() == null ? null : diagram.getSchema().getName();
        return new DiagramFile(DiagramFile.FORMAT_VERSION, diagram.getName(), diagram.getComponentSet().getName(),
                schema, blocks, connectors);
    }

    private static DiagramFile.EndData end(Port port) {
        return new DiagramFile.EndData(port.getOwner().getId(), port.getSide(), port.getOffset());
    }

    private Diagram toDiagram(DiagramFile data) {
        if (data.formatVersion() != DiagramFile.FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported format version " + data.formatVersion());
        }
        ComponentSet componentSet = resources.loadComponentSet(data.componentSet())
                .orElseThrow(IllegalArgumentException::new);
        Schema schema = data.schema() == null ? null
                : resources.loadSchema(data.schema()).orElseThrow(IllegalArgumentException::new);
        Diagram diagram = new Diagram(data.name(), componentSet, schema);
        Map<String, Block> blocks = new HashMap<>();
        for (DiagramFile.BlockData saved : data.blocks()) {
            Block block = new Block(saved.type(), saved.x(), saved.y(), saved.style().toStyle());
            block.resize(saved.width(), saved.height());
            block.setLabel(saved.label());
            saved.ports().forEach(port -> block.getPorts().add(new Port(block, port.side(), port.offset())));
            blocks.put(saved.id(), block);
            diagram.addComponent(block);
        }
        for (DiagramFile.ConnectorData saved : data.connectors()) {
            Connector connector = new Connector(saved.type(), saved.style().toStyle());
            connector.connect(port(blocks, saved.source()), port(blocks, saved.target()));
            saved.points().forEach(point ->
                    connector.getInflectionPoints().add(new InflectionPoint(point.x(), point.y())));
            diagram.addComponent(connector);
        }
        return diagram;
    }

    private static Port port(Map<String, Block> blocks, DiagramFile.EndData end) {
        Block block = blocks.get(end.blockId());
        if (block == null) {
            throw new IllegalArgumentException("Unknown block " + end.blockId());
        }
        return block.getPorts().stream()
                .filter(port -> port.getSide().equals(end.side()) && port.getOffset() == end.offset())
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    private static DiagramFileException invalidFormat(String path) {
        return new DiagramFileException("File '" + path + "' has an invalid or unsupported format");
    }
}
