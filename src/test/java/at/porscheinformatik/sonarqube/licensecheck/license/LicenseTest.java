package at.porscheinformatik.sonarqube.licensecheck.license;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LicenseTest
{
    private static final String LICENSES_JSON =
        "[{\"name\":\"Apache 2.0\",\"identifier\":\"Apache-2.0\",\"status\":\"true\"}," +
            "{\"name\":\"MIT License\",\"identifier\":\"MIT\",\"status\":\"false\"}]";
    private static final String LICENSES_STRING = "Apache 2.0~Apache-2.0~true;MIT License~MIT~false;";

    private static final License LIC1 = new License("Apache 2.0", "Apache-2.0", "true");
    private static final License LIC2 = new License("MIT License", "MIT", "false");

    @Test
    public void createString()
    {
        String dependenciesJson = License.createString(asList(LIC2, LIC1));

        assertEquals(dependenciesJson, LICENSES_JSON);
    }

    @Test
    public void fromStringOld()
    {
        List<License> licenses = License.fromString(LICENSES_STRING);

        assertTrue(licenses.containsAll(new ArrayList<>(){{
            add(LIC1);
            add(LIC2);}})
        );
    }

    @Test
    public void fromStringNew()
    {
        List<License> licenses = License.fromString(LICENSES_JSON);

        assertTrue(licenses.containsAll(new ArrayList<>(){{
            add(LIC1);
            add(LIC2);}})
        );
    }
}
