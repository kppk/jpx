package org.jpx.project;

import org.jpx.util.IOUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * TODO: Document this
 */
public interface JavaHomeSupplier extends Supplier<Path> {

    static JavaHomeSupplier getJavaHomeSupplier(JDK jdk) {
        return () -> MACOS_JAVA_HOME_PROVIDER.apply(jdk);
    }

    Function<JDK, Path> MACOS_JAVA_HOME_PROVIDER = jdk -> {
        String versionString = (jdk == JDK.v8) ? "1.8" : jdk.release;
        try {
            Process process = new ProcessBuilder()
                    .command("/usr/libexec/java_home",
                            "-v",
                            versionString)
                    .redirectErrorStream(false)
                    .start();
            return IOUtil.readAll(process).stream()
                    .findFirst()
                    .map(Paths::get)
                    .orElseThrow(() -> new IllegalArgumentException("Can't find java home for JDK " + jdk.release));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    };


}
