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
        return Manifest.readFrom(path);
    }

    @Override
    public String fetch(Path dir) {
        try {
            JavaProject libProject = new JavaProject(Manifest.readFrom(path))
                    .compile()
                    .doc()
                    .pack();

            Path library = libProject.getLibrary();
            Path targetLibrary = dir.resolve(library.getFileName());
            if (Files.exists(targetLibrary)) {
                Files.delete(targetLibrary);
            }
            Files.copy(library, targetLibrary);
            // TODO: improve this
            byte[] b = Files.readAllBytes(targetLibrary);
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            return printHexBinary(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

}
