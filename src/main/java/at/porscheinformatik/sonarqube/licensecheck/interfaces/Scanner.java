package at.porscheinformatik.sonarqube.licensecheck.interfaces;

import java.io.File;
import java.util.Set;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;

public interface Scanner
{
    Set<Dependency> scan(File moduleDir);
}
