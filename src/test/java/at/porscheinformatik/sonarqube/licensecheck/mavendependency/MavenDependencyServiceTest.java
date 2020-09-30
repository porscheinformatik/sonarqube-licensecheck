package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.ALLOWED_DEPENDENCIES_KEY;

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
        MavenDependencyService mavenDependencyService = new MavenDependencyService(Mockito.mock(
            Configuration.class));

        Assert.assertEquals(0, mavenDependencyService.getMavenDependencies().size());

        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.get(ALLOWED_DEPENDENCIES_KEY)).thenReturn(Optional.of("[{\"nameMatches\": \"NameMatches\", \"license\": \"License\"}]"));

        mavenDependencyService = new MavenDependencyService(configuration);

        Assert.assertEquals(1, mavenDependencyService.getMavenDependencies().size());
    }
}
