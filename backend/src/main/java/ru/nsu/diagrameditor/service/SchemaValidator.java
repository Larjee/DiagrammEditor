package ru.nsu.diagrameditor.service;

import org.springframework.stereotype.Component;
import ru.nsu.diagrameditor.model.Diagram;

@Component
public class SchemaValidator {

    public boolean canAddBlock(Diagram diagram, String blockType) {
        return diagram.getSchema().isBlockAllowed(blockType);
    }
}
