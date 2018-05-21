package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;
import org.jpx.project.JavaProject;
import org.jpx.util.Types;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Resolves from path in the local filesystem.
 */
public final class PathResolver implements Resolver {

    private static final String KEY_PATH = "path";

    private final Path path;

    PathResolver(Manifest mf, Dep dep) {
        String pathString = Types.safeCast(dep.values.get(KEY_PATH), String.class);
        if (pathString == null) {
            throw new IllegalArgumentException("Missing path");
        }
        path = mf.basedir.resolve(Paths.get(pathString));
    }

    PathResolver(Path path) {
        this.path = path;
    }

    public static boolean canResolve(Dep dep) {
        return dep.values.containsKey(KEY_PATH);
    }

    public static boolean canResolve(String string) {
        return string.startsWith(KEY_PATH + "+");
    }

    @Override
    public String toString() {
        return String.format("%s+file://%s", KEY_PATH, path);
    }

    public static PathResolver fromString(String string) {
        if (canResolve(string)) {
            try {
                URL url = new URL(string.substring(5));
                return new PathResolver(Paths.get(url.getPath()));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return null;
    }

    @Override
    public Manifest resolve() {
        return Manifest.readFrom(path.resolve(Manifest.NAME));
    }

    @Override
    public String fetch(Path dir) {
        JavaProject libProject = new JavaProject(Manifest.readFrom(path.resolve(Manifest.NAME)))
                .compile()
                .doc()
                .pack();

        try {
            Files.copy(libProject.getLibrary(), dir);
            // TODO: improve this
            byte[] b = Files.readAllBytes(Paths.get("/path/to/file"));
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            return new String(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
