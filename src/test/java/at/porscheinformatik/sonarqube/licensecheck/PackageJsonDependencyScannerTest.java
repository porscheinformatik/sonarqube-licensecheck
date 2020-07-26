package at.porscheinformatik.sonarqube.licensecheck;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PackageJsonDependencyScannerTest
{
    private final File folder = new File("src/test/resources");

    @Test
    public void testHappyPath()
    {
        Scanner scanner = new PackageJsonDependencyScanner(false);

        Set<Dependency> dependencies = scanner.scan(folder);

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0")));
    }

    @Test
    public void testTransitive()
    {
        Scanner scanner = new PackageJsonDependencyScanner(true);

        Set<Dependency> dependencies = scanner.scan(folder);

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
            new Dependency("angular", "1.5.0", "MIT"),
            new Dependency("arangojs", "5.6.0", "Apache-2.0"),
            new Dependency("linkedlist", "1.0.1", "LGPLv3"),
            new Dependency("retry", "0.10.1", "MIT")));
    }

    @Test
    public void testNoPackageJson()
    {
        Scanner scanner = new PackageJsonDependencyScanner(false);

        Set<Dependency> dependencies = scanner.scan(new File("src"));

        assertThat(dependencies, hasSize(0));
    }

    @Test
    public void testNoNodeModules()
    {
        Scanner scanner = new PackageJsonDependencyScanner(false);

        Set<Dependency> dependencies = scanner.scan(new File(folder, "node_modules/arangojs"));

        assertThat(dependencies, hasSize(0));
    }

    @Test
    public void testLicenseInDeprecatedLicenseFormat()
    {
        final Scanner scanner = new PackageJsonDependencyScanner(false);

        final Set<Dependency> dependencies = scanner.scan(new File(folder, "deprecated_project"));

        assertEquals(1, dependencies.size());

        final Dependency expectedDependency = new Dependency("dynamic-dedupe", "0.3.0", "MIT");
        assertEquals(expectedDependency, dependencies.toArray()[0]);
    }

    @Test
    public void testLicenseInDeprecatedLicensesFormat()
    {
        final Scanner scanner = new PackageJsonDependencyScanner(false);

        final Set<Dependency> dependencies = scanner.scan(new File(folder, "deprecated_multilicense_project"));

        assertEquals(1, dependencies.size());

        final Dependency expectedDependency = new Dependency("some-module", "1.7.1", "(MIT OR LGPLv3)");
        assertEquals(expectedDependency, dependencies.toArray()[0]);
    }
}
