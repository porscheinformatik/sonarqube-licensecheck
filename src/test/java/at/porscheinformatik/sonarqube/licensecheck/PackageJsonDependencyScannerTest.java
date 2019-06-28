package at.porscheinformatik.sonarqube.licensecheck;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.MapSettings;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class PackageJsonDependencyScannerTest
{
    private final File folder = new File("src/test/resources");

    @Test
    public void testHappyPath()
    {
        Scanner scanner = new PackageJsonDependencyScanner(new MapSettings());

        List<Dependency> dependencies = scanner.scan(folder);

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, contains(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0")));
    }

    @Test
    public void testTransitive()
    {
        Settings settings = new MapSettings();
        settings.addProperties(Collections.singletonMap(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITVE_DEPS, "true"));

        Scanner scanner = new PackageJsonDependencyScanner(settings);

        List<Dependency> dependencies = scanner.scan(folder);

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, contains(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0"),
            new Dependency("linkedlist", "1.0.1", "LGPLv3"),
            new Dependency("retry", "0.10.1", "MIT")));
    }
}
