package kppk.jpx.project;

import kppk.jpx.sys.Executor;
import kppk.jpx.sys.SysCommand;

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
        SysCommand javaHome = SysCommand.builder("/usr/libexec/java_home")
                .addParameter("-v")
                .addParameter(versionString)
                .build();
        String ret = Executor.executeAndReadAll(javaHome);
        if (ret.length() == 0) {
            throw new IllegalArgumentException("Can't find java home for JDK " + jdk.release);
        }
        return Paths.get(ret.trim());
    };


}
