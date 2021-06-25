package at.porscheinformatik.sonarqube.licensecheck;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.sonar.api.Plugin;

public class LicenseCheckPluginTest
{
    @Test
    public void define()
    {
        LicenseCheckPlugin plugin = new LicenseCheckPlugin();
        plugin.define(mock(Plugin.Context.class));
    }
}
