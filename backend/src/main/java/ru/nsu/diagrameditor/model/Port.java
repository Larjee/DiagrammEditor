package ru.nsu.diagrameditor.model;

import java.util.ArrayList;
import java.util.List;

public class Port {

    private final Block owner;
    private final String side;
    private final double offset;
    private final List<Connector> connectors = new ArrayList<>();

    public Port(Block owner, String side, double offset) {
        this.owner = owner;
        this.side = side;
        this.offset = offset;
    }

    public void attach(Connector connector) {
        if (!connectors.contains(connector)) {
            connectors.add(connector);
        }
    }

    public void detach(Connector connector) {
        connectors.remove(connector);
    }

    public Block getOwner() {
        return owner;
    }

    public String getSide() {
        return side;
    }

    public double getOffset() {
        return offset;
    }

    public List<Connector> getConnectors() {
        return List.copyOf(connectors);
    }
}
