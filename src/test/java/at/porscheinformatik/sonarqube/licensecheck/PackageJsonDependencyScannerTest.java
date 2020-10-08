package at.porscheinformatik.sonarqube.licensecheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependency;
import at.porscheinformatik.sonarqube.licensecheck.npm.PackageJsonDependencyScanner;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class PackageJsonDependencyScannerTest
{
    private final File folder = new File("src/test/resources");

    @Test
    public void testHappyPath()
    {
        Scanner scanner = new PackageJsonDependencyScanner(false);

        Set<Dependency> dependencies = scanner.scan(folder);

        assertEquals(2, dependencies.size());
        Assert.assertTrue(dependencies.containsAll(new ArrayList<PackageJsonDependency>() {
            {
                add(new PackageJsonDependency("angular", "1.5.0", "MIT"));
                add(new PackageJsonDependency("arangojs", "5.6.0", "Apache-2.0"));
            }
        }));
    }

    @Test
    public void testTransitive()
    {
        Scanner scanner = new PackageJsonDependencyScanner(true);

        Set<Dependency> dependencies = scanner.scan(folder);

        assertEquals(4, dependencies.size());
        assertTrue(dependencies.containsAll(new ArrayList<PackageJsonDependency>() {
            {
                add(new PackageJsonDependency("angular", "1.5.0", "MIT"));
                add(new PackageJsonDependency("arangojs", "5.6.0", "Apache-2.0"));
                add(new PackageJsonDependency("linkedlist", "1.0.1", "LGPLv3"));
                add(new PackageJsonDependency("retry", "0.10.1", "MIT"));
            }
        }));
    }

    @Test
    public void testNoPackageJson()
    {
        Scanner scanner = new PackageJsonDependencyScanner(false);

        Set<Dependency> dependencies = scanner.scan(new File("src"));

        assertEquals(0, dependencies.size());
    }

    @Test
    public void testNoNodeModules()
    {
        Scanner scanner = new PackageJsonDependencyScanner(false);

        Set<Dependency> dependencies = scanner.scan(new File(folder, "node_modules/arangojs"));

        assertEquals(0, dependencies.size());
    }

    @Test
    public void testLicenseInDeprecatedLicenseFormat()
    {
        final Scanner scanner = new PackageJsonDependencyScanner(false);

        final Set<Dependency> dependencies = scanner.scan(new File(folder, "deprecated_project"));

        assertEquals(1, dependencies.size());

        final Dependency expectedDependency = new PackageJsonDependency("dynamic-dedupe", "0.3.0", "MIT");
        assertEquals(expectedDependency, dependencies.toArray()[0]);
    }

    @Test
    public void testLicenseInDeprecatedLicensesFormat()
    {
        final Scanner scanner = new PackageJsonDependencyScanner(false);

        final Set<Dependency> dependencies = scanner.scan(new File(folder, "deprecated_multilicense_project"));

        assertEquals(1, dependencies.size());

        final Dependency expectedDependency = new PackageJsonDependency("some-module", "1.7.1", "(MIT OR LGPLv3)");
        assertEquals(expectedDependency, dependencies.toArray()[0]);
    }
}
