package kppk.jpx.dep;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.model.Dep;
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

    private final Dep dep;
    private final Resolver delegate;

    FileCache(Dep dep, Resolver delegate) {
        this.dep = Objects.requireNonNull(dep);
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
                Files.createDirectories(path.getParent());
                Files.write(localPath, rawFile.getBytes());
            } catch (IOException e) {
                throw new IllegalStateException("Error handling cached file ", e);
            }
        }
        return null;
    }

    @Override
    public void fetch(Version version, Path targetDir) {
        Path path = toLocalPath(PACK_DIR, version);
        if (!Files.exists(path.resolve(Manifest.NAME))) {
            delegate.fetch(version, path);
        }
        IOUtil.copy(path, targetDir.resolve(dep.name.org).resolve(dep.name.repo));
    }

    private Path toLocalPath(String dir, Version version) {
        return JPXConfig.INSTANCE.home.resolve(dir).resolve(dep.name.org).resolve(dep.name.repo).resolve(version.toString());
    }
}
