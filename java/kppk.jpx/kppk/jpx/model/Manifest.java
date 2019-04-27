package kppk.jpx.model;

import kppk.jpx.toml.Toml;
import kppk.jpx.util.Types;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manifest is project's jpx.toml file.
 */
public final class Manifest {

    public static final String NAME = "jpx.toml";

    public final Pack pack;
    public final URI basedir;

    private Manifest(Pack pack, URI basedir) {
        this.pack = pack;
        this.basedir = basedir;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Manifest{");
        sb.append("pack=").append(pack);
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
        return Objects.equals(pack, manifest.pack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pack);
    }

    private static Manifest read(URI basedir, Map<String, Object> map) {
        Pack pack = null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            switch (entry.getKey()) {
                case "pack":
                    pack = Pack.read(Types.castToMap(entry.getValue()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown key: " + entry.getKey());
            }
        }
        return new Manifest(pack, basedir);
    }

    private Map<String, Object> write() {
        HashMap<String, Object> vals = new HashMap<>();
        vals.put("pack", pack.write());
        return vals;
    }


}
