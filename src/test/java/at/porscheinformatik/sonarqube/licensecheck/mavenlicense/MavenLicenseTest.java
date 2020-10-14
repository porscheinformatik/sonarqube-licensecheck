package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

public class MavenLicenseTest
{

    @Test
    public void fromString()
    {
        String regex = "Regex";
        String licenseNameRegex = "LicenseNameRegex";
        String license = "License";
        String mavenLicenseString = "[{\"regex\": \"" + regex + "\", \"license\": \"" + license + "\"}]".replaceAll(",",
            LicenseService.COMMA_PLACEHOLDER);

        Assert.assertEquals(0, MavenLicense.fromString("").size());
        Assert.assertEquals(1, MavenLicense.fromString(mavenLicenseString).size());

        mavenLicenseString = "[{\"licenseNameRegEx\": \"" + licenseNameRegex + "\", \"license\": \"" + license + "\"}]".replaceAll(",",
            LicenseService.COMMA_PLACEHOLDER);
        Assert.assertEquals(1, MavenLicense.fromString(mavenLicenseString).size());

        Assert.assertTrue(MavenLicense.fromString(mavenLicenseString).contains(new MavenLicense(licenseNameRegex, license)));
    }

    @Test
    public void createString()
    {
        StringWriter jsonString = new StringWriter();
        JsonGenerator generator = Json.createGenerator(jsonString);
        generator.writeStartArray();
        generator.writeEnd();
        generator.close();

        Assert.assertEquals(MavenLicense.createString(new ArrayList<>()), jsonString.toString());

        Collection<MavenLicense> mavenLicenses = new ArrayList<>();
        String regex = "Regex" + RandomStringUtils.randomAlphanumeric(5);
        String license = "License" + RandomStringUtils.randomAlphanumeric(5);
        mavenLicenses.add(new MavenLicense(regex, license));

        Assert.assertTrue(MavenLicense.createString(mavenLicenses).contains(new MavenLicense(regex, license).getLicense()));
        Assert.assertTrue(MavenLicense.createString(mavenLicenses).contains(new MavenLicense(regex, license).getRegex().toString()));
    }
}
