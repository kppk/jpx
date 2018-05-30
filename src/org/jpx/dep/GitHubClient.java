package org.jpx.dep;

import org.jpx.json.JSONDocument;
import org.jpx.json.JSONFactory;
import org.jpx.json.JSONReader;
import org.jpx.model.Manifest;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * TODO: Document this
 */
class GitHubClient {

    private GitHubClient() {
    }

    private static HttpURLConnection openGETConnection(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "jpx");
        con.setConnectTimeout(10_000);
        con.setReadTimeout(10_000);
        con.setInstanceFollowRedirects(true);
        return con;
    }

    public static List<String> getTags(String user, String project) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(String.format("https://api.github.com/repos/%s/%s/tags", user, project));
            con = openGETConnection(url);
            int status = con.getResponseCode();
            if (status != 200) {
                throw new IllegalStateException("Unable to get tags from GitHub, response code: " + status);
            }
            JSONDocument response = readResponseAsJson(con);
            return parseTags(response);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static Manifest getManifest(String user, String project, String version) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, project, version, Manifest.NAME));
            con = openGETConnection(url);
            int status = con.getResponseCode();
            if (status != 200) {
                throw new IllegalStateException("Unable to get tags from GitHub, response code: " + status);
            }
            return Manifest.readFrom(url.toURI(), readResponseAsString(con));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static void fetchZipball(String user, String project, String version, Path targetDir) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(String.format("https://github.com/%s/%s/zipball/%s/", user, project, version));
            con = openGETConnection(url);
            int status = con.getResponseCode();
            if (status != 200) {
                throw new IllegalStateException("Unable to get zipball from GitHub, response code: " + status +
                        " body: " + readResponseAsString(con));
            }
            try (ZipInputStream stream = new ZipInputStream(con.getInputStream())) {
                ZipEntry entry;
                byte[] buffer = new byte[2048];
                while ((entry = stream.getNextEntry()) != null) {
                    String name = entry.getName();
                    name = name.split("/", 2)[1];
                    Path filePath = targetDir.resolve(name);
                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                        continue;
                    }
                    try (FileOutputStream output = new FileOutputStream(filePath.toFile())) {
                        int len;
                        while ((len = stream.read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> parseTags(JSONDocument doc) {
        if (!doc.isArray()) {
            throw new IllegalStateException("Unexpected GitHub json response, expected array");
        }
        return doc.array().stream()
                .map(o -> {
                    if (!(o instanceof JSONDocument)) {
                        throw new IllegalStateException("Unexpected GitHub json response, expected array of objects");
                    }
                    if (!((JSONDocument) o).isObject()) {
                        throw new IllegalStateException("Unexpected GitHub json response, expected array of objects");
                    }
                    return ((JSONDocument) o).getString("name");
                })
                .collect(Collectors.toList());
    }

    private static JSONDocument readResponseAsJson(HttpURLConnection con) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            JSONReader reader = JSONFactory.instance().makeReader(in);
            return reader.build();
        }
    }

    private static String readResponseAsString(HttpURLConnection con) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                content.append("\n");
            }
            return content.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println(getTags("kppk", "somelibrary"));
    }


}
