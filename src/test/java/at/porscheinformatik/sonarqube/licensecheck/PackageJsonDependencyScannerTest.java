package at.porscheinformatik.sonarqube.licensecheck;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.MapSettings;

import static org.hamcrest.Matchers.contains;

public class PackageJsonDependencyScannerTest
{
    final File folder = new File("src/test/resources");

    @Test
    public void testHappyPath()
    {
        Scanner scanner = new PackageJsonDependencyScanner();

        List<Dependency> dependencies = scanner.scan(folder);

        Assert.assertThat(dependencies, contains(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0")));
    }

    @Ignore
    @Test
    public void testTransitive()
    {
        Settings settings = new MapSettings();
        settings.addProperties(Collections.singletonMap(LicenseCheckPropertyKeys.NPM_RESOLVE_TRANSITVE_DEPS, "true"));

        Scanner scanner = new PackageJsonDependencyScanner();

        List<Dependency> dependencies = scanner.scan(folder);

        Assert.assertThat(dependencies, contains(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0"),
            new Dependency("linkedlist", "5.6.0", "Apache-2.0"),
            new Dependency("retry", "5.6.0", "Apache-2.0")));
    }
}
