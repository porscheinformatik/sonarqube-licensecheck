package at.porscheinformatik.sonarqube.licensecheck.licensemapping;

import at.porscheinformatik.sonarqube.licensecheck.utils.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public class LicenseMappingTest {
    @Test
    public void loadFromJson() throws IOException {
        String licenseMappingListString =
            IOUtils.readToString(LicenseMapping.class.getResourceAsStream("default_license_mapping.json"));
        List<LicenseMapping> licenseMappings = LicenseMapping.fromString(licenseMappingListString);

        assertThat(licenseMappings, hasItem(new LicenseMapping("Apple License", "AML")));
    }

    @Test
    public void compare() {
        LicenseMapping lm = new LicenseMapping("b", "A");
        LicenseMapping lm2 = new LicenseMapping("a", "B");
        assertThat(lm.compareTo(lm2), lessThan(0));
    }

    @Test
    public void hash() {
        HashSet<LicenseMapping> set = new HashSet<>();
        set.add(new LicenseMapping("x", "X"));
        set.add(new LicenseMapping("x", "X"));

        assertThat(set.size(), is(1));
    }
}
