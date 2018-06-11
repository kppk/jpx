package org.jpx.sys;

import org.jpx.json.JSONDocument;
import org.jpx.json.JSONFactory;
import org.jpx.json.JSONReader;
import org.jpx.util.IOUtil;

import java.io.BufferedReader;
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

            Path target = Files.createTempFile("jpx", "curl");
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

}
