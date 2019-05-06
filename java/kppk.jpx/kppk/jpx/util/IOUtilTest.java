package kppk.jpx.util;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit test of {@link IOUtil}.
 */
public final class IOUtilTest {

    public static void main(String[] args) throws Exception {
        testSha256dir();
    }

    public static void testSha256dir() throws Exception {

        // setup test data
        Path tmp = Files.createTempDirectory("testSha256");
        tmp.toFile().deleteOnExit();
        Path topDir = tmp.resolve("testSha256");
        topDir.toFile().deleteOnExit();
        Files.createDirectory(topDir);

        Path file1 = topDir.resolve("file1.txt");
        file1.toFile().deleteOnExit();
        Files.write(file1, "this is some test file1 content".getBytes());

        Path file2 = topDir.resolve("file2.txt");
        file2.toFile().deleteOnExit();
        Files.write(file2, "this is some test file2 content".getBytes());

        // check directory hash
        String hash = IOUtil.sha256dir(topDir);
        assert "8650114660D5CF660254DD23880D4781885EDB030F60CB9258B78360D5CBD00F".equals(hash);

        // test hash is unchanged if there is an empty dir
        Path empty = topDir.resolve("emptyDir");
        Files.createDirectory(empty);
        empty.toFile().deleteOnExit();
        String hash1 = IOUtil.sha256dir(topDir);
        assert hash.equals(hash1);

        // test hash is different if file content is changed
        Files.write(file2, "this is some test file2 content changed".getBytes());
        String hash2 = IOUtil.sha256dir(topDir);
        assert !hash.equals(hash2);

    }

}
