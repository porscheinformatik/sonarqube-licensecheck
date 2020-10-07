package at.porscheinformatik.sonarqube.licensecheck.npm;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;

public class PackageJsonDependency extends Dependency
{
    public PackageJsonDependency(final String name, final String version, final String license) {
        super(name, version, license);
    }
}
