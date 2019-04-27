package kppk.jpx.dep;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.model.Manifest;
import kppk.jpx.util.IOUtil;
import kppk.jpx.version.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Caches all the resolved files` in ~/.jpx/.pack
 */
class FileCache implements Resolver {

    private static final String PACK_DIR = ".pack";
    private static final String MANIFEST_DIR = ".manifest";

    private final String org;
    private final String repo;
    private final Resolver delegate;

    FileCache(String org, String repo, Resolver delegate) {
        this.org = Objects.requireNonNull(org);
        this.repo = Objects.requireNonNull(repo);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public List<Version> listVersions() {
        return delegate.listVersions();
    }

    @Override
    public Manifest getManifest(Version version) {
        Path path = toLocalPath(MANIFEST_DIR, version);
        if (!Files.exists(path.resolve(Manifest.NAME))) {
            Manifest mf = delegate.getManifest(version);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IllegalStateException("Can't create directory ", e);
            }
            mf.writeTo(path);
            return mf;
        }
        return Manifest.readFrom(path);
    }

    @Override
    public String getRawFile(Version version, Path path) {
        Path localPath = toLocalPath(MANIFEST_DIR, version);
        localPath = localPath.resolve(path);
        if (!Files.exists(localPath)) {
            String rawFile = delegate.getRawFile(version, path);
            try {
                Files.createDirectories(localPath.getParent());
                Files.write(localPath, rawFile.getBytes());
                return rawFile;
            } catch (IOException e) {
                throw new IllegalStateException("Error handling cached file ", e);
            }
        }
        try {
            return new String(Files.readAllBytes(localPath));
        } catch (IOException e) {
            throw new IllegalStateException("Can't read from cached file", e);
        }
    }

    @Override
    public void fetch(Version version, Path targetDir) {
        Path path = toLocalPath(PACK_DIR, version);
        if (!Files.exists(path.resolve(Manifest.NAME))) {
            delegate.fetch(version, path);
        }
        IOUtil.copy(path, targetDir.resolve(org).resolve(repo));
    }

    private Path toLocalPath(String dir, Version version) {
        return JPXConfig.INSTANCE.home.resolve(dir).resolve(org).resolve(repo).resolve(version.toString());
    }
}
