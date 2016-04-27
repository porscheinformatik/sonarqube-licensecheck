package at.porscheinformatik.sonarqube.licensecheck;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckMetrics.*;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.*;
import java.util.List;

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
                LOGGER.info("Combining dependencies on {}", context.getComponent().getKey());
                Measure measure = context.getMeasure(INPUTDEPENDENCY.getKey());
                String dependencies = "";
                if (measure != null)
                {
                    dependencies = measure.getStringValue();
                }
                for (Measure childMeasure : context.getChildrenMeasures(INPUTDEPENDENCY.getKey()))
                {
                    dependencies += childMeasure.getStringValue();
                }
                List<Dependency> dependencyList = Dependency.fromString(dependencies);
                dependencies = Dependency.createString(dependencyList);

                context.addMeasure(DEPENDENCY.getKey(), dependencies);
                LOGGER.info("Stored {}", context.getMeasure(DEPENDENCY.getKey()));

                LOGGER.info("Combining licenses on {}", context.getComponent().getKey());
                String licenses = "";
                measure = context.getMeasure(INPUTLICENSE.getKey());
                if (measure != null)
                {
                    licenses = measure.getStringValue();
                }
                for (Measure childMeasure : context.getChildrenMeasures(INPUTLICENSE.getKey()))
                {
                    licenses += childMeasure.getStringValue();
                }
                List<License> licenseList = License.fromString(licenses);
                licenses = License.createString(licenseList);

                context.addMeasure(LICENSE.getKey(), licenses);
                LOGGER.info("Stored {}", context.getMeasure(LICENSE.getKey()));
            }
        }
        else
        {
            LOGGER.info("Scanner is set to inactive. No scan possible.");
        }
    }
}