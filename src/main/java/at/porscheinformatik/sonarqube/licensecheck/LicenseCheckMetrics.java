package at.porscheinformatik.sonarqube.licensecheck;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

public class LicenseCheckMetrics implements Metrics
{
    public static final String LICENSE_CHECK_DEPENDENCY_KEY = "licensecheck.dependency";
    private static final String LICENSE_CHECK_LICENSE_KEY = "licensecheck.license";

    public static final Metric<String> DEPENDENCY =
        new Metric.Builder(LICENSE_CHECK_DEPENDENCY_KEY, "License Check - Dependencies", Metric.ValueType.DATA)
            .setDescription("Used Dependencies")
            .setDirection(Metric.DIRECTION_WORST)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    public static final Metric<String> LICENSE =
        new Metric.Builder(LICENSE_CHECK_LICENSE_KEY, "License Check - Licenses", Metric.ValueType.DATA)
            .setDescription("Used Libraries")
            .setDirection(Metric.DIRECTION_WORST)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    public static final Metric<Integer> NO_LICENSES =
        new Metric.Builder("licensecheck.no_licenses", "Number of Licenses", Metric.ValueType.INT)
            .setDescription("Number of licences of all used dependencies")
            .setDomain("Dependencies")
            .create();

    public static final Metric<Integer> NO_LICENSES_FORBIDDEN =
        new Metric.Builder("licensecheck.no_licenses_forbidden", "Number of Forbidden Licenses", Metric.ValueType.INT)
            .setDescription("Number of forbidden licences (of all used dependencies)")
            .setDomain("Dependencies")
            .create();

    public static final Metric<Integer> NO_DEPENDENCIES =
        new Metric.Builder("licensecheck.no_dependencies", "Number of Dependencies", Metric.ValueType.INT)
            .setDescription("Number of used dependencies")
            .setDomain("Dependencies")
            .create();

    public static final Metric<Integer> NO_DEPENDENCIES_WITH_FORBIDDEN_LICENSE =
        new Metric.Builder("licensecheck.no_dependencies_forbidden", "Dependencies with Forbidden License",
            Metric.ValueType.INT)
            .setDescription("Number of used dependencies with forbidden licenses")
            .setDomain("Dependencies")
            .create();

    public static final Metric<Integer> NO_DEPENDENCIES_WITH_UNKNOWN_LICENSE =
        new Metric.Builder("licensecheck.no_dependencies_unknown", "Dependencies with Unknown License",
            Metric.ValueType.INT)
            .setDescription("Number of used dependencies with unknown licenses")
            .setDomain("Dependencies")
            .create();

    @Override
    public List<Metric> getMetrics()
    {
        return Arrays.asList(DEPENDENCY, LICENSE, NO_DEPENDENCIES, NO_LICENSES, NO_LICENSES_FORBIDDEN,
            NO_DEPENDENCIES_WITH_FORBIDDEN_LICENSE, NO_DEPENDENCIES_WITH_UNKNOWN_LICENSE);
    }
}
