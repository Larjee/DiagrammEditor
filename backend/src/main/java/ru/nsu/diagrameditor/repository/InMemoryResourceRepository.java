package ru.nsu.diagrameditor.repository;

import org.springframework.stereotype.Repository;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.ConnectionRule;
import ru.nsu.diagrameditor.model.Schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryResourceRepository implements ResourceRepository {

    private final List<ComponentSet> componentSets = List.of(
            new ComponentSet("uml-class",
                    List.of("class", "interface", "enum"),
                    List.of("association", "inheritance", "realization", "dependency", "aggregation", "composition")),
            new ComponentSet("flowchart",
                    List.of("start", "end", "process", "decision", "input-output"),
                    List.of("flow"))
    );

    private final List<Schema> schemas = List.of(
            new Schema("uml-class",
                    List.of("class", "interface", "enum"),
                    List.of("association", "inheritance", "realization", "dependency"),
                    Map.of("class", 8, "interface", 8, "enum", 4),
                    List.of(
                            new ConnectionRule("inheritance", "class", "class"),
                            new ConnectionRule("inheritance", "interface", "interface"),
                            new ConnectionRule("realization", "class", "interface"),
                            new ConnectionRule("association", "class", "class"),
                            new ConnectionRule("dependency", "class", "class"),
                            new ConnectionRule("dependency", "class", "interface")),
                    Map.of(),
                    Map.of()),
            new Schema("flowchart",
                    List.of("start", "end", "process", "decision", "input-output"),
                    List.of("flow"),
                    Map.of("start", 1, "end", 1, "decision", 3),
                    List.of(
                            new ConnectionRule("flow", "start", "process"),
                            new ConnectionRule("flow", "process", "process"),
                            new ConnectionRule("flow", "process", "decision"),
                            new ConnectionRule("flow", "decision", "process"),
                            new ConnectionRule("flow", "process", "input-output"),
                            new ConnectionRule("flow", "input-output", "process"),
                            new ConnectionRule("flow", "process", "end")),
                    Map.of(),
                    Map.of())
    );

    @Override
    public List<ComponentSet> getComponentSets() {
        return componentSets;
    }

    @Override
    public List<Schema> getSchemas() {
        return schemas;
    }

    @Override
    public Optional<ComponentSet> loadComponentSet(String name) {
        return componentSets.stream().filter(set -> set.getName().equals(name)).findFirst();
    }

    @Override
    public Optional<Schema> loadSchema(String name) {
        return schemas.stream().filter(schema -> schema.getName().equals(name)).findFirst();
    }
}
