package org.jpx.model;

import org.jpx.toml.Toml;
import org.jpx.util.Types;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
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
    public final URI basedir;

    private Manifest(Pack pack, List<Dep> deps, URI basedir) {
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

    public static Manifest readFrom(Path dir) {
        Objects.requireNonNull(dir, "dir");

        try {
            Map<String, Object> toml = Toml.read(dir.resolve(NAME).toFile());
            return read(dir.toAbsolutePath().normalize().toUri(), toml);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Manifest readFrom(URI uri, String content) {
        Map<String, Object> toml = Toml.read(content);
        return read(uri, toml);
    }

    public static Manifest readFrom(URI uri, Reader reader) {
        Map<String, Object> toml = null;
        try {
            toml = Toml.read(reader, 2048, true);
            return read(uri, toml);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Manifest manifest = (Manifest) o;
        return Objects.equals(pack, manifest.pack) &&
                Objects.equals(deps, manifest.deps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pack, deps);
    }

    private static Manifest read(URI basedir, Map<String, Object> map) {
        Pack pack = null;
        List<Dep> deps = Collections.emptyList();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case "pack":
                    pack = Pack.read(Types.castToMap(entry.getValue()));
                    break;
                case "deps":
                    deps = readDeps(entry.getValue());
                    break;
            }
        }
//        Types.checkRequired(err -> {
//            throw new IllegalArgumentException(err.stream()
//                    .map(entry -> entry.getKey() + ": " + entry.getKey())
//                    .collect(Collectors.joining(",")));
//        }, Types.pair("pack", pack));
        return new Manifest(pack, deps, basedir);
    }

    private static List<Dep> readDeps(Object obj) {
        List<Dep> deps = new LinkedList<>();
        for (Map.Entry<String, Object> entry : Types.castToMap(obj).entrySet()) {
            deps.add(Dep.read(entry));
        }
        return deps;
    }

    private Map<String, Object> write() {
        HashMap<String, Object> vals = new HashMap<>();
        vals.put("pack", pack.write());
        vals.put("deps", deps.stream()
                .map(Dep::write)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return vals;
    }


}
