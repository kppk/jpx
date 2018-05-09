package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TODO: Document this
 */
public class PathResolver implements Resolver {


    @Override
    public Manifest resolveManifest(Manifest parent, Dep dep) {
        if (dep.path == null) {
            throw new IllegalArgumentException("Missing path");
        }
        Path path = parent.basedir.resolve(Paths.get(dep.path, Manifest.NAME));
        return Manifest.readFrom(path);
    }

}
