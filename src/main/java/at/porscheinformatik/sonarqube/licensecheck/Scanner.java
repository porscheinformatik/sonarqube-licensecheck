package at.porscheinformatik.sonarqube.licensecheck;

import java.io.File;
import java.util.Set;

public interface Scanner
{
    Set<Dependency> scan(File moduleDir);
}
