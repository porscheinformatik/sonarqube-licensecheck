package at.porscheinformatik.sonarqube.licensecheck.licensemapping;

import java.io.IOException;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import at.porscheinformatik.sonarqube.licensecheck.utils.IOUtils;

public class LicenseMappingTest
{
    @Test
    public void loadFromJson() throws IOException
    {
        String licenseMappingListString =
            IOUtils.readToString(LicenseMapping.class.getResourceAsStream("default_license_mapping.json"));
        List<LicenseMapping> licenseMappings = LicenseMapping.fromString(licenseMappingListString);

        assertThat(licenseMappings, hasItem(new LicenseMapping("Apple License", "AML")));
    }
}
