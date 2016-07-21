package at.porscheinformatik.sonarqube.licensecheck;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.DEPENDENCY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.INPUTDEPENDENCY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.INPUTLICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.LICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.MAVENLICENSE;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ACTIVATION_KEY;

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
            if (context.getComponent().getType() == Component.Type.PROJECT)
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

    private void combineLicenses(MeasureComputerContext context)
    {
        LOGGER.debug("Combining licenses on {}", context.getComponent().getKey());
        List<License> licensesWithChildren = new ArrayList<>();
        Measure measure = context.getMeasure(INPUTLICENSE.getKey());
        if (measure != null && StringUtils.isNotBlank(measure.getStringValue()))
        {
            licensesWithChildren.addAll(License.fromString(measure.getStringValue()));
        }
        for (Measure childMeasure : context.getChildrenMeasures(INPUTLICENSE.getKey()))
        {
            if (childMeasure != null && StringUtils.isNotBlank(childMeasure.getStringValue()))
            {
                licensesWithChildren.addAll(License.fromString(measure.getStringValue()));
            }
        }
        context.addMeasure(LICENSE.getKey(), License.createString(licensesWithChildren));
        LOGGER.debug("Stored {}", context.getMeasure(LICENSE.getKey()));
    }

    private void combineDependencies(MeasureComputerContext context)
    {
        LOGGER.debug("Combining dependencies on {}", context.getComponent().getKey());
        Measure measure = context.getMeasure(INPUTDEPENDENCY.getKey());
        List<Dependency> dependencyWithChildren = new ArrayList<>();
        if (measure != null && StringUtils.isNotBlank(measure.getStringValue()))
        {
            dependencyWithChildren.addAll(Dependency.fromString(measure.getStringValue()));
        }
        for (Measure childMeasure : context.getChildrenMeasures(INPUTDEPENDENCY.getKey()))
        {
            if (childMeasure != null && StringUtils.isNotBlank(childMeasure.getStringValue()))
            {
                dependencyWithChildren.addAll(Dependency.fromString(measure.getStringValue()));
            }
        }
        context.addMeasure(DEPENDENCY.getKey(), Dependency.createString(dependencyWithChildren));
    }
}