package kppk.jpx.sys;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.json.JSONDocument;
import kppk.jpx.json.JSONFactory;
import kppk.jpx.json.JSONReader;
import kppk.jpx.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * TODO: Document this
 */
public class Curl {

    public static Path get(String url) {
        Objects.requireNonNull(url);

        try {
            Files.createDirectories(JPXConfig.INSTANCE.tmpDir);
            Path target = JPXConfig.INSTANCE.tmpDir.resolve("curl_" + randomString());
            File targetFile = target.toFile();
            boolean created = targetFile.createNewFile();
            if (!created) {
                throw new IllegalStateException("Can't create temp file [" + target.toAbsolutePath() + "]");
            }
            targetFile.deleteOnExit();

            target.toFile().deleteOnExit();
            SysCommand curl = SysCommand.builder("curl")
                    .addParameter("-sSL")
                    .addParameter("-o")
                    .addParameter(target.toAbsolutePath().toString())
                    .addParameter("-w")
                    .addParameter("%{http_code}")
                    .addParameter(url)
                    .build();

            String response = Executor.executeAndReadAll(curl);

            if (!"200".equals(response)) {
                throw new IllegalStateException("Got error response: " + response);
            }
            return target;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static JSONDocument getAsJson(String url) {
        Objects.requireNonNull(url);

        try {
            Path out = get(url);

            try (Reader r = new BufferedReader(new FileReader(out.toFile()))) {
                JSONReader reader = JSONFactory.instance().makeReader(r);
                return reader.build();
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getAsString(String url) {
        Objects.requireNonNull(url);

        Path out = get(url);
        try (Reader r = new BufferedReader(new FileReader(out.toFile()))) {
            return IOUtil.readAll(r);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static String randomString() {
        int count = 5;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
