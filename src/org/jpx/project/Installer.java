package org.jpx.project;

import org.jpx.config.JPXConfig;
import org.jpx.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TODO: Document this
 */
public class Installer {

    private static final String BIN_DIR = "bin";
    private static final String PACKAGE_DIR = "binaries";

    public void installBinary(String name, String version, Path srcDirectory, Path executable) {
        Path bin = JPXConfig.INSTANCE.home.resolve(BIN_DIR);
        Path pkg = JPXConfig.INSTANCE.home.resolve(PACKAGE_DIR);

        Path destDir = pkg.resolve(name).resolve(version);
        Path destDirBin = destDir.resolve(executable);
        Path destBin = bin.resolve(binaryName(name));

        if (Files.exists(destDir)) {
            IOUtil.removeDir(destDir);
        }

        try {

            if (Files.isSymbolicLink(destBin)) {
                Files.delete(destBin);
            }

            Files.createDirectories(destDir);
            Files.createDirectories(bin);
            IOUtil.copy(srcDirectory, destDir);
            Files.createSymbolicLink(destBin, destDirBin);
            patchBinaryScript(destDirBin);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    private static void patchBinaryScript(Path exec) {
        try {
            String orig = new String(Files.readAllBytes(exec));
            String patched = orig.replace("DIR=`dirname $0`", "DIR=" + exec.getParent().toAbsolutePath().toString());
            if (!orig.equals(patched)) {
                Files.write(exec, patched.getBytes());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String binaryName(String name) {
        int idx = name.lastIndexOf("/");
        if (idx > 0) {
            return name.substring(idx + 1);
        }
        return name;
    }

}
