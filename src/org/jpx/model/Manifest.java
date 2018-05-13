package org.jpx.model;

import org.jpx.toml.Toml;
import org.jpx.util.Types;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class Manifest {

    public static final String NAME = "jpx.toml";

    public final Pack pack;
    public final List<Dep> deps;
    public final Path basedir;

    private Manifest(Pack pack, List<Dep> deps, Path basedir) {
        this.pack = pack;
        this.deps = deps;
        this.basedir = basedir;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Manifest{");
        sb.append("pack=").append(pack);
        sb.append(", deps=").append(deps);
        sb.append('}');
        return sb.toString();
    }

    public static Manifest readFrom(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            Map<String, Object> toml = Toml.read(path.toFile());
            return parse(path.toAbsolutePath().getParent(), toml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Manifest parse(Path dir, Map<String, Object> map) {
        Pack pack = null;
        List<Dep> deps = null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case "pack":
                    pack = Pack.parse(Types.castToMap(entry.getValue(), String.class, Object.class));
                    break;
                case "deps":
                    deps = parseDeps(entry.getValue());
                    break;
            }
        }
        Types.checkRequired(err -> {
            throw new IllegalArgumentException(err.stream()
                    .map(entry -> entry.getKey() + ": " + entry.getKey())
                    .collect(Collectors.joining(",")));
        }, Types.pair("pack", pack));
        return new Manifest(pack, deps, dir);
    }

    private static List<Dep> parseDeps(Object obj) {
        List<Dep> deps = new LinkedList<>();
        for (Map.Entry<String, Object> entry : Types.castToMap(obj, String.class, Object.class).entrySet()) {
            deps.add(Dep.parse(entry));
        }
        return deps;
    }


}
