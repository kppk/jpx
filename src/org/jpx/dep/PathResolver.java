package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;
import org.jpx.util.Types;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves from path in the local filesystem.
 */
public final class PathResolver implements Resolver {

    private static final String KEY_PATH = "path";

    private final Path baseDir;

    PathResolver(Manifest mf) {
        this.baseDir = mf.basedir;
    }

    public static boolean canResolve(Dep dep) {
        return dep.values.containsKey(KEY_PATH);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PathResolver{");
        sb.append("baseDir=").append(baseDir);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public Manifest resolve(Dep dep) {
        String pathString = Types.safeCast(dep.values.get(KEY_PATH), String.class);
        if (pathString == null) {
            throw new IllegalArgumentException("Missing path");
        }
        Path path = baseDir.resolve(Paths.get(pathString, Manifest.NAME));
        return Manifest.readFrom(path);
    }

}
