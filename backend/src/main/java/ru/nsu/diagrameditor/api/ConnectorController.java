package ru.nsu.diagrameditor.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.diagrameditor.model.Connector;
import ru.nsu.diagrameditor.model.Port;
import ru.nsu.diagrameditor.service.DiagramEditor;
import ru.nsu.diagrameditor.service.InvalidRequestException;

@RestController
@RequestMapping("/api/diagram/connectors")
public class ConnectorController {

    private final DiagramEditor editor;

    public ConnectorController(DiagramEditor editor) {
        this.editor = editor;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConnectorResponse addConnector(@RequestBody AddConnectorRequest request) {
        Port source = port(request.source());
        Port target = port(request.target());
        Connector connector = editor.addConnector(request.connectorType(), source, target);
        return ConnectorResponse.from(connector);
    }

    @PostMapping("/{id}/points")
    @ResponseStatus(HttpStatus.CREATED)
    public ConnectorResponse addInflectionPoint(@PathVariable String id, @RequestBody Point point) {
        return ConnectorResponse.from(editor.addInflectionPoint(id, point.x(), point.y()));
    }

    @PutMapping("/{id}/points/{index}")
    public ConnectorResponse moveInflectionPoint(@PathVariable String id, @PathVariable int index,
                                                 @RequestBody Point point) {
        return ConnectorResponse.from(editor.moveInflectionPoint(id, index, point.x(), point.y()));
    }

    private Port port(PortRef ref) {
        if (ref == null) {
            throw new InvalidRequestException("Source and target ports are required");
        }
        return editor.findPort(ref.blockId(), ref.side(), ref.offset());
    }

    public record AddConnectorRequest(String connectorType, PortRef source, PortRef target) {
    }
}
