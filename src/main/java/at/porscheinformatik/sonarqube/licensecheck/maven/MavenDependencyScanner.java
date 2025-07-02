package at.porscheinformatik.sonarqube.licensecheck.maven;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.maven.model.License;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

public class MavenDependencyScanner implements Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyScanner.class);
    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";

    private final LicenseMappingService licenseMappingService;

    public MavenDependencyScanner(LicenseMappingService licenseMappingService) {
        this.licenseMappingService = licenseMappingService;
    }

    @Override
    public Set<Dependency> scan(SensorContext context) {
        MavenSettings settings = getSettingsFromCommandLineArgs();

        FileSystem fs = context.fileSystem();
        FilePredicate pomXmlPredicate = fs.predicates().matchesPathPattern("**/pom.xml");

        Set<Dependency> allDependencies = new HashSet<>();

        for (InputFile pomXml : fs.inputFiles(pomXmlPredicate)) {
            context.markForPublishing(pomXml);

            LOGGER.info("Scanning for Maven dependencies (POM: {})", pomXml.uri());
            try (
                Stream<Dependency> dependencies = readDependencyList(
                    new File(pomXml.uri()),
                    settings
                )
            ) {
                dependencies
                    .map(this.loadLicenseFromPom(licenseMappingService.getLicenseMap(), settings))
                    .forEach(dependency -> {
                        dependency.setInputComponent(pomXml);
                        dependency.setTextRange(pomXml.newRange(1, 0, pomXml.lines(), 0));
                        allDependencies.add(dependency);
                    });
            }
        }

        return allDependencies;
    }

    private static Stream<Dependency> readDependencyList(File pomXml, MavenSettings settings) {
        Path tempFile = createTempFile();
        if (tempFile == null) {
            return Stream.empty();
        }

        InvocationRequest request = new DefaultInvocationRequest();
        request.setRecursive(false);
        request.setPomFile(pomXml);
        request.setBaseDirectory(pomXml.getParentFile());
        request.setGoals(Collections.singletonList("dependency:list"));
        if (settings.userSettings != null) {
            request.setUserSettingsFile(new File(settings.userSettings));
            LOGGER.info("Using user settings {}", settings.userSettings);
        }
        if (settings.globalSettings != null) {
            request.setGlobalSettingsFile(new File(settings.globalSettings));
            LOGGER.info("Using global settings {}", settings.globalSettings);
        }
        Properties properties = new Properties();
        properties.setProperty("outputFile", tempFile.toAbsolutePath().toString());
        properties.setProperty("outputAbsoluteArtifactFilename", "true");
        properties.setProperty("includeScope", "runtime"); // only runtime (scope compile + runtime)
        if (System.getProperty(MAVEN_REPO_LOCAL) != null) {
            properties.setProperty(MAVEN_REPO_LOCAL, System.getProperty(MAVEN_REPO_LOCAL));
        }
        if (System.getenv("MAVEN_OPTS") != null) {
            request.setMavenOpts(System.getenv("MAVEN_OPTS"));
        }
        request.setBatchMode(true);
        request.setProperties(properties);

        return invokeMaven(request, tempFile);
    }

    private static Stream<Dependency> invokeMaven(InvocationRequest request, Path mavenOutputFile) {
        try {
            StringBuilder mavenExecutionErrors = new StringBuilder();
            Invoker invoker = new DefaultInvoker();
            if (System.getProperty("maven.home") != null) {
                invoker.setMavenHome(new File(System.getProperty("maven.home")));
            } else if (System.getenv("MAVEN_HOME") != null) {
                invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
            } else {
                String mvnPath = getMvnPath();
                if (mvnPath != null) {
                    invoker.setMavenHome(new File(mvnPath).getParentFile().getParentFile());
                } else {
                    LOGGER.warn("Could not find mvn in path");
                }
            }
            request.setOutputHandler(line -> {
                if (line.startsWith("[ERROR] ")) {
                    mavenExecutionErrors.append(line.substring(8)).append(System.lineSeparator());
                }
            });
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                LOGGER.warn(
                    "Could not get dependency list via maven",
                    result.getExecutionException()
                );
                LOGGER.warn(mavenExecutionErrors.toString());
            }
            return Files
                .lines(mavenOutputFile)
                .filter(StringUtils::isNotBlank)
                .map(MavenDependencyScanner::findDependency)
                .filter(Objects::nonNull);
        } catch (MavenInvocationException e) {
            LOGGER.warn("Could not get dependency list via maven", e);
        } catch (Exception e) {
            LOGGER.warn("Error reading file", e);
        }
        return Stream.empty();
    }

    private static String getMvnPath() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String locateCommand = isWindows ? "where" : "which";
        String mvnCommand = isWindows ? "mvn.cmd" : "mvn";
        ProcessBuilder processBuilder = new ProcessBuilder(locateCommand, mvnCommand);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line = reader.readLine();
            reader.close();
            return line;
        } catch (IOException e) {
            LOGGER.warn("Could not find mvn in path: {}", e.getMessage());
            return null;
        }
    }

    private static Path createTempFile() {
        try {
            Path tempFile = Files.createTempFile("dependencies", ".txt");
            tempFile.toFile().deleteOnExit();
            return tempFile;
        } catch (IOException e) {
            LOGGER.error("Could not create temp file for dependencies: {}", e.getMessage());
            return null;
        }
    }

    static Dependency findDependency(String line) {
        String[] items = getItems(line);
        if (items == null) return null;

        String groupId = items[0];
        String artifactId = items[1];
        String version = items[3];
        String path = items[5];

        String classifier = null;
        if (items.length > 6) {
            classifier = items[3];
            version = items[4];
            path = items[6];
        }

        if (classifier != null) {
            path = path.replace("-" + classifier, "");
        }
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > 0) {
            path = path.substring(0, lastDotIndex) + ".pom";
        }

        Dependency dependency = new Dependency(
            groupId + ":" + artifactId,
            version,
            null,
            LicenseCheckRulesDefinition.LANG_JAVA
        );
        if (new File(path).exists()) {
            dependency.setPomPath(path);
        }
        return dependency;
    }

    private static String[] getItems(String line) {
        // Remove module info introduced with Maven Dependency Plugin 3.0 (and JDK > 9)
        line = line.replaceFirst(" -- module .*", "");

        String[] items = line.trim().split(":");
        if (items.length < 4) {
            return null;
        }

        // Windows-specific absolute path "C:\my\path"
        if (items[items.length - 2].length() == 1) {
            items[items.length - 2] += ":" + items[items.length - 1];
            String[] newItems = new String[items.length - 1];
            System.arraycopy(items, 0, newItems, 0, items.length - 1);
            items = newItems;
        }

        return items;
    }

    private Function<Dependency, Dependency> loadLicenseFromPom(
        Map<Pattern, String> licenseMap,
        MavenSettings settings
    ) {
        return (Dependency dependency) -> {
            if (
                StringUtils.isNotBlank(dependency.getLicense()) || dependency.getPomPath() == null
            ) {
                return dependency;
            }

            return loadLicense(licenseMap, settings, dependency);
        };
    }

    private static Dependency loadLicense(
        Map<Pattern, String> licenseMap,
        MavenSettings settings,
        Dependency dependency
    ) {
        String pomPath = dependency.getPomPath();
        if (pomPath != null) {
            List<License> licenses = LicenseFinder.getLicenses(
                new File(pomPath),
                settings.userSettings,
                settings.globalSettings
            );
            if (licenses.isEmpty()) {
                LOGGER.info("No licenses found in dependency {}", dependency.getName());
                return dependency;
            }

            for (License license : licenses) {
                boolean found = licenseMatcher(licenseMap, dependency, license);
                if (found) {
                    break;
                }
            }
        }
        return dependency;
    }

    /**
     * @return true if license was found in defined license list, false otherwise
     */
    private static boolean licenseMatcher(
        Map<Pattern, String> licenseMap,
        Dependency dependency,
        License license
    ) {
        String licenseName = license.getName();
        // Fallback: if licenseName is blank, use license.getUrl()
        if (StringUtils.isBlank(licenseName)) {
            licenseName = license.getUrl();
            if (StringUtils.isBlank(licenseName)) {
                LOGGER.info("Dependency '{}' has an empty license and url.", dependency.getName());
                return false;
            }
        }
        for (Entry<Pattern, String> entry : licenseMap.entrySet()) {
            if (entry.getKey().matcher(licenseName).matches()) {
                dependency.setLicense(entry.getValue());
                return true;
            }
        }
        dependency.setLicense(licenseName);
        LOGGER.info(
            "No licenses match found for '{}' in dependency '{}:{}'",
            licenseName,
            dependency.getName(),
            dependency.getVersion()
        );
        return false;
    }

    private static MavenSettings getSettingsFromCommandLineArgs() {
        String globalSettings = null;
        String userSettings = null;
        String commandArgs = System.getProperty("sun.java.command");
        try (java.util.Scanner scanner = new java.util.Scanner(commandArgs)) {
            while (scanner.hasNext()) {
                String part = scanner.next();
                if (part.equals("-gs") || part.equals("--global-settings")) {
                    globalSettings = scanner.next();
                } else if (part.equals("-s") || part.equals("--settings")) {
                    userSettings = scanner.next();
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Ignore unparsable command line", e);
        }
        return new MavenSettings(globalSettings, userSettings);
    }
}

class MavenSettings {

    final String globalSettings;
    final String userSettings;

    MavenSettings(String globalSettings, String userSetttings) {
        this.globalSettings = globalSettings;
        this.userSettings = userSetttings;
    }
}
