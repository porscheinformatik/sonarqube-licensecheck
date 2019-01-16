package at.porscheinformatik.sonarqube.licensecheck.integration;

import java.io.File;
import java.io.IOException;

class GradleWrapperResolver {

    private static final String DEFAULT_GRADLE_VERSION = "5.1.1";

    static int loadGradleWrapper(File projectRoot) throws IOException {
        return loadGradleWrapper(projectRoot, DEFAULT_GRADLE_VERSION);
    }

    static int loadGradleWrapper(File projectRoot, String version) throws IOException {
        String[] command = {"gradle", "wrapper", "--gradle-version=" + version};
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = processBuilder.command(command).directory(projectRoot).start();
        while (process.isAlive()) {
        }
        return process.exitValue();
    }
}
