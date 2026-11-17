package ru.nsu.diagrameditor.model;

import java.util.ArrayList;
import java.util.List;

public class Connector extends Component {

    private Port source;
    private Port target;
    private final List<InflectionPoint> inflectionPoints = new ArrayList<>();

    public Connector(String type, Style style) {
        super(type, style);
    }

    public void connect(Port sourcePort, Port targetPort) {
        if (source != null) {
            source.detach(this);
        }
        if (target != null) {
            target.detach(this);
        }
        source = sourcePort;
        target = targetPort;
        source.attach(this);
        target.attach(this);
    }

    public void moveInflectionPoint(InflectionPoint point, double x, double y) {
        if (!inflectionPoints.contains(point)) {
            throw new IllegalArgumentException("Inflection point does not belong to this connector");
        }
        point.moveTo(x, y);
    }

    public List<InflectionPoint> getInflectionPoints() {
        return inflectionPoints;
    }

    @Override
    public void resize(double width, double height) {
        throw new UnsupportedOperationException("Connector size is defined by its ports");
    }

    public Port getSource() {
        return source;
    }

    public Port getTarget() {
        return target;
    }
}
