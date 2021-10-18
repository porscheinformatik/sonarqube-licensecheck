package at.porscheinformatik.sonarqube.licensecheck;

import java.util.Set;

import org.sonar.api.batch.sensor.SensorContext;

public interface Scanner
{
    Set<Dependency> scan(SensorContext context);
}
