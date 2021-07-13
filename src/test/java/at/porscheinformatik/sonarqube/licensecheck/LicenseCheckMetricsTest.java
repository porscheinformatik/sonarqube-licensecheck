package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class LicenseCheckMetricsTest
{
    @Test
    public void getMetrics()
    {
        assertThat(new LicenseCheckMetrics().getMetrics(), notNullValue());
    }
}
