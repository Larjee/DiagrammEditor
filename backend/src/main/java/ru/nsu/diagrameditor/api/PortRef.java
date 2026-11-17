package ru.nsu.diagrameditor.api;

import ru.nsu.diagrameditor.model.Port;

public record PortRef(String blockId, String side, double offset) {

    static PortRef from(Port port) {
        return new PortRef(port.getOwner().getId(), port.getSide(), port.getOffset());
    }
}
