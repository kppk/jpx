package org.jpx.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * TODO: Document this
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
                                Files.copy(s, d);
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

}
