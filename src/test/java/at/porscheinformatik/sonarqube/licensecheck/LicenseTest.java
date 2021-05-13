package at.porscheinformatik.sonarqube.licensecheck;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import at.porscheinformatik.sonarqube.licensecheck.license.License;

public class LicenseTest
{
    private static final String LICENSES_JSON =
        "[{\"name\":\"Apache 2.0\",\"identifier\":\"Apache-2.0\",\"status\":\"true\"}," +
            "{\"name\":\"MIT License\",\"identifier\":\"MIT\",\"status\":\"false\"}]";
    private static final String LICENSES_STRING = "Apache 2.0~Apache-2.0~true;MIT License~MIT~false;";

    private static final License LIC1 = new License("Apache 2.0", "Apache-2.0", "true");
    private static final License LIC2 = new License("MIT License", "MIT", "false");

    @Test
    public void createJsonString()
    {
        String dependenciesJson = License.createJsonString(asList(LIC2, LIC1));

        assertThat(dependenciesJson, equalTo(LICENSES_JSON));
    }

    @Test
    public void fromStringOld()
    {
        List<License> licenses = License.fromString(LICENSES_STRING);

        assertThat(licenses, hasItems(LIC1, LIC2));
    }

    @Test
    public void fromStringNew()
    {
        List<License> licenses = License.fromString(LICENSES_JSON);

        assertThat(licenses, hasItems(LIC1, LIC2));
    }
}
