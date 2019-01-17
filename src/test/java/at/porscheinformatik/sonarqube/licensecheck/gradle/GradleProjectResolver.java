package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

public class GradleProjectResolver {

    private static final String DEFAULT_GRADLE_VERSION = "5.1.1";
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleProjectResolver.class);

    public static void loadGradleWrapper(File projectRoot) throws IOException {
        loadGradleWrapper(projectRoot, DEFAULT_GRADLE_VERSION);
    }

    public static void loadGradleWrapper(File projectRoot, String version) throws IOException {
        String[] command = {projectRoot.getAbsolutePath() + "/gradlew", "wrapper", "--gradle-version=" + version};
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = processBuilder.command(command).directory(projectRoot).start();
        while (process.isAlive()) {
        }
        if (process.exitValue() != 0) {
            String stdout = IOUtils.toString(process.getInputStream(), "UTF_8");
            LOGGER.error(stdout);
            throw new RuntimeException("Failed downloading gradle wrapper");
        }
    }

    public static File prepareGradleProject() throws IOException {
        File projectRoot;
        projectRoot = new File("target/testProject");
        FileUtils.deleteDirectory(projectRoot);
        projectRoot.mkdirs();
        File buildGradleSrc = new File(GradleProjectResolver.class.getClassLoader().getResource("gradle/build.gradle").getPath()).getParentFile();
        FileUtils.copyDirectory(buildGradleSrc, projectRoot);
        File gradlewTrg = new File(projectRoot, "gradlew");
        Files.setPosixFilePermissions(gradlewTrg.toPath(), PosixFilePermissions.fromString("rwxr-xr-x"));
        return projectRoot.getAbsoluteFile();
    }
}
