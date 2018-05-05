package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.gradle.license.LicenseMatcher;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static at.porscheinformatik.sonarqube.licensecheck.gradle.TestDataBuilder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class PomDependenyMapperTest {

    private Model pom;

    @Before
    public void setup() throws IOException, XmlPullParserException {
        pom = pom("my-artifact", "com.sample", "1.0",
            licenses(license("The Apache Software License 2.0")));
    }

    @Test
    public void matcherIsNull() {
        PomDependencyMapper pomDependencyMapper = new PomDependencyMapper(null);
        Dependency dependency = pomDependencyMapper.toDependency(pom);

        Dependency expected = new Dependency("com.sample:my-artifact", "1.0", "The Apache Software License 2.0");

        Assert.assertEquals(expected, dependency);
    }

    @Test
    public void matcherMatches() {
        LicenseMatcher licenseMatcher = Mockito.mock(LicenseMatcher.class);
        when(licenseMatcher.viaLicenseMap("The Apache Software License 2.0")).thenReturn("Apache-2.0");

        PomDependencyMapper pomDependencyMapper = new PomDependencyMapper(licenseMatcher);
        Dependency dependency = pomDependencyMapper.toDependency(pom);

        Dependency expected = new Dependency("com.sample:my-artifact", "1.0", "Apache-2.0");

        Assert.assertEquals(expected, dependency);
    }

    @Test
    public void returnFirstLicenseIfThereIsNoMatch() {
        LicenseMatcher licenseMatcher = Mockito.mock(LicenseMatcher.class);
        when(licenseMatcher.viaLicenseMap(any())).thenReturn("");

        PomDependencyMapper pomDependencyMapper = new PomDependencyMapper(licenseMatcher);
        Dependency dependency = pomDependencyMapper.toDependency(pom);

        Dependency expected = new Dependency("com.sample:my-artifact", "1.0", "The Apache Software License 2.0");

        Assert.assertEquals(expected, dependency);
    }

    @Test
    public void resolveBlankGroupAndVersionFromParent() {
        Model pomWithParent = pom("my-artifact", null, "",
            parent("com.sample", "1.1"),
            licenses(license("The Apache Software License 2.0")));

        PomDependencyMapper pomDependencyMapper = new PomDependencyMapper(null);
        Dependency dependency = pomDependencyMapper.toDependency(pomWithParent);

        Dependency expected = new Dependency("com.sample:my-artifact", "1.1", "The Apache Software License 2.0");

        Assert.assertEquals(expected, dependency);
    }
}
