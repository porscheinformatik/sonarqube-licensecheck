package at.porscheinformatik.sonarqube.licensecheck.utils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class IOUtilsTest
{
    @Test
    public void testLoadFile() throws IOException
    {
        String packageJson = IOUtils.readToString(IOUtilsTest.class.getResourceAsStream("/package.json"));
        assertThat(packageJson, containsString("\"name\": \"test\""));
    }
}
