package org.jpx.dep;

import org.jpx.model.Manifest;
import org.jpx.version.Version;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Resolves from path in the local filesystem.
 */
public final class PathResolver implements Resolver {

    private static final String KEY_PATH = "path";

    private final Path path;

//    PathResolver(Manifest mf, Dep dep) {
//        String pathString = Types.safeCast(dep.values.get(KEY_PATH), String.class);
//        if (pathString == null) {
//            throw new IllegalArgumentException("Missing path");
//        }
//        path = Paths.get(mf.basedir).resolve(Paths.get(pathString));
//    }

    PathResolver(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return String.format("%s+file://%s", KEY_PATH, path);
    }

    public static PathResolver fromString(String string) {
        try {
            URL url = new URL(string.substring(5));
            return new PathResolver(Paths.get(url.getPath()));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Override
    public List<Version> listVersions() {
        return null;
    }

    @Override
    public Manifest getManifest(Version version) {
        return null;
    }

    @Override
    public void fetch(Version version, Path targetDir) {

    }

    public Manifest resolve() {
        return Manifest.readFrom(path);
    }

    public String fetch(Path dir) {
        return null;
//        try {
//            JavaProject libProject = JavaProject.createNew(Manifest.readFrom(path))
//                    .build()
//                    .doc()
//                    .pack();
//
//            Path library = libProject.getLibrary();
//            Path targetLibrary = dir.resolve(library.getFileName());
//            if (Files.exists(targetLibrary)) {
//                Files.delete(targetLibrary);
//            }
//            Files.copy(library, targetLibrary);
//            // TODO: improve this
//            byte[] b = Files.readAllBytes(targetLibrary);
//            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
//            return printHexBinary(hash);
//        } catch (IOException | NoSuchAlgorithmException e) {
//            throw new IllegalStateException(e);
//        }
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
