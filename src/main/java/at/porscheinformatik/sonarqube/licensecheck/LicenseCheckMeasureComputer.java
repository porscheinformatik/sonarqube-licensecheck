package at.porscheinformatik.sonarqube.licensecheck;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.DEPENDENCY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.INPUTDEPENDENCY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.INPUTLICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.MAVENLICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ACTIVATION_KEY;
import static org.sonar.api.ce.measure.Component.Type.MODULE;
import static org.sonar.api.ce.measure.Component.Type.PROJECT;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.config.Settings;

import at.porscheinformatik.sonarqube.licensecheck.license.License;

public class LicenseCheckMeasureComputer implements MeasureComputer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseCheckMeasureComputer.class);
    private final Settings settings;

    public LicenseCheckMeasureComputer(Settings settings)
    {
        this.settings = settings;
    }

    @Override
    public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext)
    {
        return defContext.newDefinitionBuilder()
            .setInputMetrics(INPUTDEPENDENCY.getKey(), INPUTLICENSE.getKey())
            .setOutputMetrics(DEPENDENCY.getKey(), LICENSE.getKey(), MAVENLICENSE.getKey())
            .build();
    }

    @Override
    public void compute(MeasureComputerContext context)
    {
        if (settings.getBoolean(ACTIVATION_KEY))
        {
            Component.Type type = context.getComponent().getType();
            if (type == MODULE || type == PROJECT)
            {
                combineDependencies(context);

                combineLicenses(context);
            }
        }
        else
        {
            LOGGER.info("Scanner is set to inactive. No scan possible.");
        }
    }

    private static void combineLicenses(MeasureComputerContext context)
    {
        LOGGER.debug("Combining licenses on {}", context.getComponent().getKey());
        List<License> licensesWithChildren = new ArrayList<>();
        Measure measure = context.getMeasure(INPUTLICENSE.getKey());
        addLicenseMeasure(licensesWithChildren, measure);
        for (Measure childMeasure : context.getChildrenMeasures(INPUTLICENSE.getKey()))
        {
            addLicenseMeasure(licensesWithChildren, childMeasure);
        }
        for (Measure childMeasure : context.getChildrenMeasures(LICENSE.getKey()))
        {
            addLicenseMeasure(licensesWithChildren, childMeasure);
        }
        context.addMeasure(LICENSE.getKey(), License.createString(licensesWithChildren));
        LOGGER.debug("Stored licenses {}", licensesWithChildren);
    }

    private static void addLicenseMeasure(final List<License> licensesWithChildren, final Measure measure)
    {
        if (measure != null && StringUtils.isNotBlank(measure.getStringValue()))
        {
            licensesWithChildren.addAll(License.fromString(measure.getStringValue()));
        }
    }

    private static void combineDependencies(MeasureComputerContext context)
    {
        LOGGER.debug("Combining dependencies on {}", context.getComponent().getKey());
        Measure measure = context.getMeasure(INPUTDEPENDENCY.getKey());
        List<Dependency> dependencyWithChildren = new ArrayList<>();
        addDependencyMeasure(dependencyWithChildren, measure);
        for (Measure childMeasure : context.getChildrenMeasures(INPUTDEPENDENCY.getKey()))
        {
            addDependencyMeasure(dependencyWithChildren, childMeasure);
        }
        for (Measure childMeasure : context.getChildrenMeasures(DEPENDENCY.getKey()))
        {
            addDependencyMeasure(dependencyWithChildren, childMeasure);
        }
        context.addMeasure(DEPENDENCY.getKey(), Dependency.createString(dependencyWithChildren));
        LOGGER.debug("Stored dependencies {}", dependencyWithChildren);
    }

    private static void addDependencyMeasure(final List<Dependency> dependencyWithChildren, final Measure measure)
    {
        if (measure != null && StringUtils.isNotBlank(measure.getStringValue()))
        {
            dependencyWithChildren.addAll(Dependency.fromString(measure.getStringValue()));
        }
    }
}