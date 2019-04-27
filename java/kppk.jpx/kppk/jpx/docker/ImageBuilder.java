package kppk.jpx.docker;

import kppk.jpx.sys.Executor;
import kppk.jpx.sys.SysCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Builds docker images, can also minimize them using `smith`.
 */
public final class ImageBuilder {

    private static final String FROM_IMAGE = "oraclelinux:7-slim";
    private static final String SMITH_IMAGE = "tjfontaine/smith:latest";

    public static void buildImage(Path workDir,
                                  Path targetDir,
                                  String jdkRelease,
                                  String imageName,
                                  String version,
                                  String binaryName,
                                  boolean minimize) {

        // write Dockerfile
        Path dockerFile = targetDir.resolve("Dockerfile");
        try {
            Files.createDirectories(targetDir);
            Files.write(dockerFile, dockerFile(jdkRelease, binaryName).getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write Dockerfile to " + targetDir);
        }

        if (imageName == null) {
            imageName = String.format("%s:%s", binaryName, version);
        }

        SysCommand dockerBuild = SysCommand.builder("docker")
                .addParameter("build")
                .addParameter(workDir.toAbsolutePath().toString())
                .addParameter("-f")
                .addParameter(dockerFile.toAbsolutePath().toString())
                .addParameter("-t")
                .addParameter(imageName)
//                .addParameter("-t")
//                .addParameter(imageName + ":latest")
                .build();
        Executor.execute(null, workDir, Collections.singletonList(dockerBuild));

        if (minimize) {
            minimize(binaryName, imageName, targetDir);
        }
    }

    private static void minimize(String pkg, String image, Path targetDir) {

        String pkgIn = String.format("%s.tgz", pkg);
        String pkgOut = String.format("%s-smith.tgz", pkg);

        SysCommand dockerSave = SysCommand.builder("docker")
                .addParameter("save")
                .addParameter(image)
                .pipeTo("gzip")
                .addParameter("-c")
                .addParameter(">")
                .addParameter(pkgIn)
                .build();
        Executor.execute(null, targetDir, Collections.singletonList(dockerSave));

        // write smith.yaml
        try {
            Files.write(targetDir.resolve("smith.yaml"),
                    smithFile(pkgIn).getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write smith.yaml to " + targetDir);
        }

        SysCommand dockerRunSmith = SysCommand.builder("docker")
                .addParameter("run")
                .addParameter("--rm")
                .addParameter("-v")
                .addParameter(targetDir.toAbsolutePath().toString() + ":/write")
                .addParameter("-v")
                .addParameter("tmp:/tmp")
                .addParameter(SMITH_IMAGE)
                .addParameter("--docker")
                .addParameter("-i")
                .addParameter(pkgOut)
                .addParameter("-t")
                .addParameter(image)
                .build();
        Executor.execute(null, targetDir, Collections.singletonList(dockerRunSmith));

        SysCommand dockerLoad = SysCommand.builder("docker")
                .addParameter("load")
                .addParameter("-i")
                .addParameter(targetDir.resolve(pkgOut).toAbsolutePath().toString())
                .build();
        Executor.execute(null, targetDir, Collections.singletonList(dockerLoad));

        // remove pkg files
        try {
            Files.deleteIfExists(targetDir.resolve(pkgIn));
            Files.deleteIfExists(targetDir.resolve(pkgOut));
        } catch (IOException e) {

        }

    }

    private static String dockerFile(String jdkRelease, String binaryName) {
        return "from " + FROM_IMAGE + " as builder\n" +
                "\n" +
                "RUN yum install -y tar gzip && yum clean all\n" +
                "RUN curl -LSs https://raw.githubusercontent.com/kppk/jpx/master/install.sh | sh\n" +
                "RUN ~/.jpx/bin/jpx java --release " + jdkRelease + "\n" +
                "\n" +
                "COPY . /work\n" +
                "WORKDIR /work\n" +
                "RUN set -e; \\\n" +
                "  ~/.jpx/bin/jpx install; \\\n" +
                "  ~/.jpx/bin/jpx build\n" +
                "\n" +
                "from " + FROM_IMAGE + "\n" +
                "COPY --from=builder /work/target/binary /app\n" +
                "CMD [\"/app/bin/" + binaryName + "\"]\n";
    }

    private static String smithFile(String image) {
        return "type: container\n" +
                "package: " + image + "\n" +
                "root: true\n" +
                "env: [\"PATH=/bin:/app/bin\"]\n" +
                "paths:\n" +
                "  - /bin/sh\n" +
                "  - /app\n" +
                "  - /usr/lib64/libm.so.6\n" +
                "  - /usr/lib64/librt.so.1\n" +
                "  - /usr/bin/dirname";
    }

}
