package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.Configuration;

import java.util.Optional;

public class MavenDependencyServiceTest
{
    @Test
    public void getMavenDependencies()
    {
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.get(ALLOWED_DEPENDENCIES_KEY)).thenReturn(Optional.of(""));
        MavenDependencyService mavenDependencyService = new MavenDependencyService(configuration);

        //we check the "default" value which is hard coded
        Assert.assertEquals(2, mavenDependencyService.getMavenDependencies().size());

        Mockito.when(configuration.get(ALLOWED_DEPENDENCIES_KEY)).thenReturn(Optional.of("[{\"nameMatches\": \"NameMatches\", \"license\": \"License\"}]".replaceAll(",",
            LicenseService.COMMA_PLACEHOLDER)));

        Assert.assertEquals(1, mavenDependencyService.getMavenDependencies().size());
    }
}
