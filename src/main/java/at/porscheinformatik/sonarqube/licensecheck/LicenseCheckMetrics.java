package at.porscheinformatik.sonarqube.licensecheck;

import static java.lang.Boolean.TRUE;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

public class LicenseCheckMetrics implements Metrics
{
    public static final String LICENSE_CHECK_KEY = "licensecheck";

    public static final String LICENSE_CHECK_UNLISTED_KEY = "licensecheck.unlisted";
    public static final String LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY = "licensecheck.notallowedlicense";

    public static final String LICENSE_CHECK_DEPENDENCY_KEY = "licensecheck.dependency";
    private static final String LICENSE_CHECK_LICENSE_KEY = "licensecheck.license";
    private static final String INPUT_DEPENDENCY_KEY = "licensecheck.inputdependency";
    private static final String INPUT_LICENSE_KEY = "licensecheck.inputlicense";


    public static final Metric DEPENDENCY =
        new Metric.Builder(LICENSE_CHECK_DEPENDENCY_KEY, "License Check - Dependencies", Metric.ValueType.DATA)
            .setDescription("Used Dependencies")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(TRUE)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    public static final Metric INPUTDEPENDENCY =
        new Metric.Builder(INPUT_DEPENDENCY_KEY, "License Check - Input Dependencies", Metric.ValueType.DATA)
            .setDescription("Used Dependencies")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(TRUE)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    public static final Metric LICENSE =
        new Metric.Builder(LICENSE_CHECK_LICENSE_KEY, "License Check - Licenses", Metric.ValueType.DATA)
            .setDescription("Used Libraries")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(TRUE)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    public static final Metric INPUTLICENSE =
        new Metric.Builder(INPUT_LICENSE_KEY, "License Check - Input Licenses", Metric.ValueType.DATA)
            .setDescription("Used Libraries")
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(TRUE)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    @Override
    public List<Metric> getMetrics()
    {
        return Arrays.asList(DEPENDENCY, LICENSE, INPUTDEPENDENCY, INPUTLICENSE);
    }
}
