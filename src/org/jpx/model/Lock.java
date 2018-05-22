package org.jpx.model;

import org.jpx.dep.Dependency;
import org.jpx.toml.Toml;
import org.jpx.util.Types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class Lock {

    private static final String NAME = "jpx.lock";

    public final List<Dependency> dependencies;
    public final Meta meta;

    public Lock(List<Dependency> dependencies, Meta meta) {
        this.dependencies = dependencies;
        this.meta = meta;
    }

    public static final class Meta {
        public final String inputDigest;

        public Meta(String inputDigest) {
            this.inputDigest = inputDigest;
        }

        private Map<String, Object> write() {
            Map<String, Object> vals = new HashMap<>(1);
            vals.put("inputDigest", inputDigest);
            return vals;
        }

        private static Meta read(Map<String, Object> vals) {
            Objects.requireNonNull(vals);
            Object val = vals.get("inputDigest");
            return new Meta(Types.safeCast(val, String.class));
        }
    }

    public static Lock readFrom(Path dir) {
        Path file = Objects.requireNonNull(dir).resolve(NAME);
        try {
            Map<String, Object> toml = Toml.read(file.toFile());
            return read(toml);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void writeTo(Path dir) {
        try {
            Path file = Objects.requireNonNull(dir).resolve(NAME);
            if (Files.notExists(file)) {
                Files.createFile(file);
            }
            Toml.write(write(), file.toFile());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> write() {
        HashMap<String, Object> vals = new HashMap<>();
        vals.put("meta", meta.write());
        vals.put("dependency", dependencies.stream()
                .map(Dependency::write)
                .collect(Collectors.toList()));
        return vals;
    }

    private static Lock read(Map<String, Object> map) {
        List<Dependency> dependencies = null;
        Meta meta = null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case "dependency":
                    dependencies = Types.castToList(entry.getValue()).stream()
                            .map(Types::castToMap)
                            .map(Dependency::read)
                            .collect(Collectors.toList());
                    break;
                case "meta":
                    meta = Meta.read(Types.castToMap(entry.getValue()));
                    break;
            }
        }
        return new Lock(dependencies, meta);
    }

}
