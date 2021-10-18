package at.porscheinformatik.sonarqube.licensecheck;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.sonar.api.Plugin;

public class LicenseCheckPluginTest
{
    @Test
    public void define()
    {
        Plugin.Context context = mock(Plugin.Context.class);
        LicenseCheckPlugin plugin = new LicenseCheckPlugin();
        plugin.define(context);

        verify(context).addExtensions(any());
    }
}
