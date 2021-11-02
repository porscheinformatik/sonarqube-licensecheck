package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;
import java.io.IOException;
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
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition;
import at.porscheinformatik.sonarqube.licensecheck.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.licensemapping.LicenseMappingService;

public class MavenDependencyScanner implements Scanner
{
    private static final Logger LOGGER = Loggers.get(MavenDependencyScanner.class);
    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";

    private final LicenseMappingService licenseMappingService;

    public MavenDependencyScanner(LicenseMappingService licenseMappingService)
    {
        this.licenseMappingService = licenseMappingService;
    }

    @Override
    public Set<Dependency> scan(SensorContext context)
    {
        MavenSettings settings = getSettingsFromCommandLineArgs();

        FileSystem fs = context.fileSystem();
        FilePredicate pomXmlPredicate = fs.predicates().matchesPathPattern("**/pom.xml");

        Set<Dependency> allDependencies = new HashSet<>();

        for (InputFile pomXml : fs.inputFiles(pomXmlPredicate))
        {
            context.markForPublishing(pomXml);

            LOGGER.info("Scanning for Maven dependencies (POM: {})", pomXml.uri());
            try (Stream<Dependency> dependencies = readDependencyList(new File(pomXml.uri()), settings))
            {
                dependencies
                    .map(this.loadLicenseFromPom(licenseMappingService.getLicenseMap(), settings))
                    .forEach(dependency ->
                    {
                        dependency.setInputComponent(pomXml);
                        dependency.setTextRange(pomXml.newRange(1, 0, pomXml.lines(), 0));
                        allDependencies.add(dependency);
                    });
            }
        }

        return allDependencies;
    }

    private static Stream<Dependency> readDependencyList(File pomXml, MavenSettings settings)
    {
        Path tempFile = createTempFile();
        if (tempFile == null)
        {
            return Stream.empty();
        }

        InvocationRequest request = new DefaultInvocationRequest();
        request.setRecursive(false);
        request.setPomFile(pomXml);
        request.setGoals(Collections.singletonList("dependency:list"));
        if (settings.userSettings != null)
        {
            request.setUserSettingsFile(new File(settings.userSettings));
            LOGGER.info("Using user settings {}", settings.userSettings);
        }
        if (settings.globalSettings != null)
        {
            request.setGlobalSettingsFile(new File(settings.globalSettings));
            LOGGER.info("Using global settings {}", settings.globalSettings);
        }
        Properties properties = new Properties();
        properties.setProperty("outputFile", tempFile.toAbsolutePath().toString());
        properties.setProperty("outputAbsoluteArtifactFilename", "true");
        properties.setProperty("includeScope", "runtime"); // only runtime (scope compile + runtime)
        if (System.getProperty(MAVEN_REPO_LOCAL) != null)
        {
            properties.setProperty(MAVEN_REPO_LOCAL, System.getProperty(MAVEN_REPO_LOCAL));
        }
        request.setProperties(properties);

        return invokeMaven(request, tempFile);
    }

    private static Stream<Dependency> invokeMaven(InvocationRequest request, Path mavenOutputFile)
    {
        try
        {
            StringBuilder mavenExecutionErrors = new StringBuilder();
            Invoker invoker = new DefaultInvoker();
            invoker.setOutputHandler(line -> {
                if (line.startsWith("[ERROR] "))
                {
                    mavenExecutionErrors
                        .append(line.substring(8))
                        .append(System.lineSeparator());
                }
            });
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0)
            {
                LOGGER.warn("Could not get dependency list via maven", result.getExecutionException());
                LOGGER.warn(mavenExecutionErrors.toString());
            }
            return Files.lines(mavenOutputFile)
                .filter(StringUtils::isNotBlank)
                .map(MavenDependencyScanner::findDependency)
                .filter(Objects::nonNull);
        }
        catch (MavenInvocationException e)
        {
            LOGGER.warn("Could not get dependency list via maven", e);
        }
        catch (Exception e)
        {
            LOGGER.warn("Error reading file", e);
        }
        return Stream.empty();
    }

    private static Path createTempFile()
    {
        try
        {
            Path tempFile = Files.createTempFile("dependencies", ".txt");
            tempFile.toFile().deleteOnExit();
            return tempFile;
        }
        catch (IOException e)
        {
            LOGGER.error("Could not create temp file for dependencies: {}", e.getMessage());
            return null;
        }
    }

    static Dependency findDependency(String line)
    {
        String[] items = getItems(line);
        if (items == null)
            return null;

        String groupId = items[0];
        String artifactId = items[1];
        String version = items[3];
        String path = items[5];

        String classifier = null;
        if (items.length > 6)
        {
            classifier = items[3];
            version = items[4];
            path = items[6];
        }

        if (classifier != null)
        {
            path = path.replace("-" + classifier, "");
        }
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > 0)
        {
            path = path.substring(0, lastDotIndex) + ".pom";
        }

        Dependency dependency =
            new Dependency(groupId + ":" + artifactId, version, null, LicenseCheckRulesDefinition.LANG_JAVA);
        if (new File(path).exists())
        {
            dependency.setPomPath(path);
        }
        return dependency;
    }

    private static String[] getItems(String line)
    {
        // Remove module info introduced with Maven Dependency Plugin 3.0 (and JDK > 9)
        line = line.replaceFirst(" -- module .*", "");

        String[] items = line.trim().split(":");
        if (items.length < 4)
        {
            return null;
        }

        // Windows-specific absolute path "C:\my\path"
        if (items[items.length - 2].length() == 1)
        {
            items[items.length - 2] += ":" + items[items.length - 1];
            String[] newItems = new String[items.length - 1];
            System.arraycopy(items, 0, newItems, 0, items.length - 1);
            items = newItems;
        }

        return items;
    }

    private Function<Dependency, Dependency> loadLicenseFromPom(Map<Pattern, String> licenseMap, MavenSettings settings)
    {
        return (Dependency dependency) ->
        {
            if (StringUtils.isNotBlank(dependency.getLicense())
                || dependency.getPomPath() == null)
            {
                return dependency;
            }

            return loadLicense(licenseMap, settings, dependency);
        };
    }

    private static Dependency loadLicense(Map<Pattern, String> licenseMap, MavenSettings settings,
        Dependency dependency)
    {
        String pomPath = dependency.getPomPath();
        if (pomPath != null)
        {
            List<License> licenses = LicenseFinder.getLicenses(new File(pomPath), settings.userSettings,
                settings.globalSettings);
            if (licenses.isEmpty())
            {
                LOGGER.info("No licenses found in dependency {}", dependency.getName());
                return dependency;
            }

            for (License license : licenses)
            {
                licenseMatcher(licenseMap, dependency, license);
            }
        }
        return dependency;
    }

    private static void licenseMatcher(Map<Pattern, String> licenseMap, Dependency dependency, License license)
    {
        String licenseName = license.getName();
        if (StringUtils.isBlank(licenseName))
        {
            LOGGER.info("Dependency '{}' has an empty license.", dependency.getName());
            return;
        }

        for (Entry<Pattern, String> entry : licenseMap.entrySet())
        {
            if (entry.getKey().matcher(licenseName).matches())
            {
                dependency.setLicense(entry.getValue());
                return;
            }
        }

        LOGGER.info("No licenses match found for '{}'", licenseName);
    }

    private static MavenSettings getSettingsFromCommandLineArgs()
    {
        String globalSettings = null;
        String userSettings = null;
        String commandArgs = System.getProperty("sun.java.command");
        try (java.util.Scanner scanner = new java.util.Scanner(commandArgs))
        {
            while (scanner.hasNext())
            {
                String part = scanner.next();
                if (part.equals("-gs") || part.equals("--global-settings"))
                {
                    globalSettings = scanner.next();
                }
                else if (part.equals("-s") || part.equals("--settings"))
                {
                    userSettings = scanner.next();
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.debug("Ignore unparsable command line", e);
        }
        return new MavenSettings(globalSettings, userSettings);
    }
}

class MavenSettings
{
    final String globalSettings;
    final String userSettings;

    MavenSettings(String globalSettings, String userSetttings)
    {
        this.globalSettings = globalSettings;
        this.userSettings = userSetttings;
    }
}
