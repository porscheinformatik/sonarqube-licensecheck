package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.License;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependency;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;

public class MavenDependencyScanner implements Scanner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyScanner.class);

    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";

    private final MavenLicenseService mavenLicenseService;

    private final MavenDependencyService mavenDependencyService;

    public MavenDependencyScanner(MavenLicenseService mavenLicenseService,
        MavenDependencyService mavenDependencyService)
    {
        this.mavenLicenseService = mavenLicenseService;
        this.mavenDependencyService = mavenDependencyService;
    }

    @Override
    public List<Dependency> scan(File moduleDir)
    {
        if(!new File(moduleDir, "pom.xml").exists())
        {
            return Collections.emptyList();
        }

        String userSettings = null;
        String globalSettings = null;
        CommandLine cmd = getCommandLineArgs();
        if (cmd != null)
        {
            if (cmd.hasOption("s"))
            {
                userSettings = cmd.getOptionValue("s");
            }
            if (cmd.hasOption("gs"))
            {
                globalSettings = cmd.getOptionValue("gs");
            }
        }

        return this.readDependecyList(moduleDir, userSettings, globalSettings)
            .map(this.loadLicenseFromPom(mavenLicenseService.getLicenseMap(), userSettings, globalSettings))
            .map(this::mapMavenDependencyToLicense)
            .collect(Collectors.toList());
    }

    private Stream<Dependency> readDependecyList(File moduleDir, String userSettings, String globalSettings)
    {
        Path tempFile = createTempFile();
        if (tempFile == null)
        {
            return Stream.empty();
        }

        InvocationRequest request = new DefaultInvocationRequest();
        request.setRecursive(false);
        request.setPomFile(new File(moduleDir, "pom.xml"));
        request.setGoals(Collections.singletonList("dependency:list"));
        if (userSettings != null)
        {
            request.setUserSettingsFile(new File(userSettings));
        }
        if (globalSettings != null)
        {
            request.setGlobalSettingsFile(new File(globalSettings));
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

        Invoker invoker = new DefaultInvoker();
        invoker.setOutputHandler(null); // not interested in Maven output itself

        try
        {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0)
            {
                LOGGER.warn("Could not get dependency list via maven", result.getExecutionException());
            }
            return Files.lines(tempFile)
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

    private Path createTempFile()
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

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("\\s*([^:]*):([^:]*):[^:]*:([^:]*):[^:]*:(.*)");
    private static Dependency findDependency(String line)
    {
        // Remove module info introduced with Maven Dependency Plugin 3.0 (and JDK > 9)
        line = line.replaceFirst(" -- module .*", "");

        Matcher matcher = DEPENDENCY_PATTERN.matcher(line);
        if (matcher.find())
        {
            String groupId = matcher.group(1);
            String artifactId = matcher.group(2);
            String version = matcher.group(3);
            String path = matcher.group(4);
            Dependency dependency = new Dependency(groupId + ":" + artifactId, version, null);
            dependency.setLocalPath(path);
            return dependency;
        }
        return null;
    }

    private Function<Dependency, Dependency> loadLicenseFromPom(Map<Pattern, String> licenseMap, String userSettings,
        String globalSettings)
    {
        return (Dependency dependency) ->
        {
            String path = dependency.getLocalPath();
            if (path == null)
            {
                return dependency;
            }

            return loadLicense(licenseMap, userSettings, globalSettings, dependency);
        };
    }

    private Dependency loadLicense(Map<Pattern, String> licenseMap, String userSettings, String globalSettings,
        Dependency dependency)
    {
        String path = dependency.getLocalPath();
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > 0)
        {
            String pomPath = path.substring(0, lastDotIndex) + ".pom";
            List<License> licenses = LicenseFinder.getLicenses(new File(pomPath), userSettings, globalSettings);
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

    private Dependency licenseMatcher(Map<Pattern, String> licenseMap, Dependency dependency, License license)
    {
        String licenseName = license.getName();
        if (StringUtils.isBlank(licenseName))
        {
            LOGGER.info("Dependency '{}' has no license set.", dependency.getName());
            return dependency;
        }

        for (Entry<Pattern, String> entry : licenseMap.entrySet())
        {
            if (entry.getKey().matcher(licenseName).matches())
            {
                dependency.setLicense(entry.getValue());
                return dependency;
            }
        }

        LOGGER.info("No licenses found for '{}'", licenseName);
        return dependency;
    }

    private Dependency mapMavenDependencyToLicense(Dependency dependency)
    {
        if (StringUtils.isBlank(dependency.getLicense()))
        {
            for (MavenDependency allowedDependency : mavenDependencyService.getMavenDependencies())
            {
                String matchString = allowedDependency.getKey();
                if (dependency.getName().matches(matchString))
                {
                    dependency.setLicense(allowedDependency.getLicense());
                }
            }
        }
        return dependency;
    }

    private static CommandLine getCommandLineArgs()
    {
        CommandLine cmd = null;
        try
        {
            String commandArgs = System.getProperty("sun.java.command");
            CommandLineParser parser = new DefaultParser();
            Options options = new Options();
            options.addOption("s", "settings", true, "Alternate path for the user settings file");
            options.addOption("gs", "global-settings", true, "Alternate path for the global settings file");
            cmd = parser.parse(options, commandArgs.split(" "));
        }
        catch (Exception e)
        {
            // ignore unparsable command line args
        }
        return cmd;
    }
}
