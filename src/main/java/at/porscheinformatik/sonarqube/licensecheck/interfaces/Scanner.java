package at.porscheinformatik.sonarqube.licensecheck.interfaces;

import java.io.File;
import java.util.List;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;

public interface Scanner
{
    public List<Dependency> scan(File moduleDir, String mavenProjectDependencies);
}
