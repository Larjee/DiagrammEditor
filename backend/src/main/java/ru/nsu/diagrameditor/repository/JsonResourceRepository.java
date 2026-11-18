package ru.nsu.diagrameditor.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import ru.nsu.diagrameditor.model.ComponentSet;
import ru.nsu.diagrameditor.model.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Repository
public class JsonResourceRepository implements ResourceRepository {

    private static final List<String> BUILT_IN = List.of("uml-class", "flowchart");
    private static final String EXTENSION = ".json";

    private final ObjectMapper mapper;
    private final Path componentSets;
    private final Path schemas;

    public JsonResourceRepository(ObjectMapper mapper, @Value("${editor.data-dir}") Path dataDir) throws IOException {
        this.mapper = mapper;
        this.componentSets = prepare(dataDir.resolve("component-sets"));
        this.schemas = prepare(dataDir.resolve("schemas"));
    }

    @Override
    public List<ComponentSet> getComponentSets() {
        return names(componentSets).map(this::loadComponentSet).flatMap(Optional::stream).toList();
    }

    @Override
    public List<Schema> getSchemas() {
        return names(schemas).map(this::loadSchema).flatMap(Optional::stream).toList();
    }

    @Override
    public Optional<ComponentSet> loadComponentSet(String name) {
        return read(componentSets, name, ComponentSetFile.class, (file, n) -> file.toComponentSet(n));
    }

    @Override
    public Optional<Schema> loadSchema(String name) {
        return read(schemas, name, SchemaFile.class, (file, n) -> file.toSchema(n));
    }

    private <F, T> Optional<T> read(Path folder, String name, Class<F> type, BiFunction<F, String, T> convert) {
        if (name == null) {
            return Optional.empty();
        }
        Path file = folder.resolve(name + EXTENSION).normalize();
        if (!folder.equals(file.getParent()) || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(convert.apply(mapper.readValue(file.toFile(), type), name));
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Stream<String> names(Path folder) {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(file -> file.endsWith(EXTENSION))
                    .map(file -> file.substring(0, file.length() - EXTENSION.length()))
                    .sorted()
                    .toList()
                    .stream();
        } catch (IOException e) {
            return Stream.empty();
        }
    }

    private static Path prepare(Path folder) throws IOException {
        Path absolute = folder.toAbsolutePath().normalize();
        if (Files.notExists(absolute)) {
            Files.createDirectories(absolute);
            for (String name : BUILT_IN) {
                String resource = "defaults/" + absolute.getFileName() + "/" + name + EXTENSION;
                try (InputStream in = new ClassPathResource(resource).getInputStream()) {
                    Files.copy(in, absolute.resolve(name + EXTENSION));
                }
            }
        }
        return absolute;
    }
}
