package kppk.jpx.cache;

import kppk.jpx.config.JPXConfig;
import kppk.jpx.dep.Resolver;
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
 * TODO: Document this
 */
public class FileCache implements Resolver {

    private static final String PACK_DIR = ".pack";
    private static final String MANIFEST_DIR = ".manifest";

    private final Dep dep;
    private final Resolver delegate;

    public FileCache(Dep dep, Resolver delegate) {
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