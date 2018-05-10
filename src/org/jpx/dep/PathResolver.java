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

    final static PathResolver INSTANCE = new PathResolver();
    private static final String KEY_PATH = "path";

    private PathResolver() {
    }

    @Override
    public boolean canResolve(Dep dep) {
        return dep.values.containsKey(KEY_PATH);
    }

    @Override
    public Manifest resolve(Manifest parent, Dep dep) {
        String pathString = Types.safeCast(dep.values.get(KEY_PATH), String.class);
        if (pathString == null) {
            throw new IllegalArgumentException("Missing path");
        }
        Path path = parent.basedir.resolve(Paths.get(pathString, Manifest.NAME));
        return Manifest.readFrom(path);
    }

}
