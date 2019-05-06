package kppk.jpx.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Misc IO related utility functions.
 */
public final class IOUtil {
    private IOUtil() {
    }

    public static String readAll(Reader reader) throws IOException {
        char[] arr = new char[8 * 1024];
        StringBuilder builder = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
            builder.append(arr, 0, numCharsRead);
        }
        return builder.toString();
    }

    public static void copy(Path src, Path dest) {
        try {
            if (!Files.isDirectory(src)) {
                Files.copy(src, dest);
                return;
            }
            Files.walk(src)
                    .forEach(s -> {
                        Path d = dest.resolve(src.relativize(s));
                        try {
                            if (Files.isDirectory(s)) {
                                Files.createDirectories(d);
                            } else {
                                Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void removeDir(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            throw new IllegalStateException("Can't delete directory", e);
        }
    }

    public static String sha256file(Path file) {
        Objects.requireNonNull(file);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return printHexBinary(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Counts SHA-256 of a directory, by traversing the directory and doing the following for each entry:
     * <ul>
     * <li>if the entry is directory and it is empty, ignore it, otherwise use it's relative path</li>
     * <li>counting sha-256 of the file</li>
     * </ul>
     * <p>
     * Then, it will count of the sha-256 of the list prepared in earlier step.
     */
    public static String sha256dir(Path dir) {
        Objects.requireNonNull(dir);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Files.walk(dir)
                    .flatMap(path -> {
                        String relative = dir.getParent().relativize(path).toString();
                        if (Files.isDirectory(path)) {
                            // if non-empty directory, include its relative name
                            return isEmpty(path) ? Stream.empty() : Stream.of(relative);
                        } else {
                            return Stream.of(relative + ":" + sha256file(path));
                        }
                    })
                    .sorted()
                    .forEach(s -> digest.update(s.getBytes()));
            return printHexBinary(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isEmpty(Path dir) {
        try {
            return !Files.walk(dir).anyMatch(path -> !path.equals(dir));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

}
