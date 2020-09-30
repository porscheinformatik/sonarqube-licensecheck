package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import jdk.nashorn.internal.runtime.CodeStore;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

public class MavenLicenseTest
{

    @Test
    public void fromString()
    {
        String regex = "Regex";
        String licenseNameRegex = "LicenseNameRegex";
        String mavenLicenseString = "[{\"regex\": \"" + regex + "\", \"licenseNameRegEx\": \"" + licenseNameRegex + "\"}]";

        Assert.assertEquals(0, MavenLicense.fromString("").size());
        Assert.assertEquals(1, MavenLicense.fromString(mavenLicenseString).size());
        Assert.assertTrue(MavenLicense.fromString(mavenLicenseString).contains(new MavenLicense(regex, licenseNameRegex)));
    }

    @Test
    public void createString()
    {
        StringWriter jsonString = new StringWriter();
        JsonGenerator generator = Json.createGenerator(jsonString);
        generator.writeStartArray();
        generator.writeEnd();
        generator.close();

        Assert.assertTrue(MavenLicense.createString(new ArrayList<>()).equals(jsonString.toString()));

        Collection<MavenLicense> mavenLicenses = new ArrayList<>();
        String regex = "Regex" + RandomStringUtils.randomAlphanumeric(5);
        String license = "License" + RandomStringUtils.randomAlphanumeric(5);
        mavenLicenses.add(new MavenLicense(regex, license));

        Assert.assertTrue(MavenLicense.createString(mavenLicenses).contains(new MavenLicense(regex, license).getLicense()));
        Assert.assertTrue(MavenLicense.createString(mavenLicenses).contains(new MavenLicense(regex, license).getRegex().toString()));
    }
}
