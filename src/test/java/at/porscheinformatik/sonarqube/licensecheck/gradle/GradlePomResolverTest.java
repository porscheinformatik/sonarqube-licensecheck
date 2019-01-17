package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static at.porscheinformatik.sonarqube.licensecheck.gradle.GradleProjectResolver.prepareGradleProject;

public class GradlePomResolverTest {


    private static File projectRoot;

    @Before
    public void setup() throws IOException {
        projectRoot = prepareGradleProject();
    }

    @Test
    public void resolvePoms() throws Exception {
        GradlePomResolver gradlePomResolver = new GradlePomResolver(projectRoot);

        List<Model> poms = gradlePomResolver.resolvePomsOfAllDependencies();

        Model pom = new Model();
        pom.setArtifactId("spock-core");
        pom.setGroupId("org.spockframework");
        pom.setVersion("1.1-groovy-2.4");
        License pomLicense = new License();
        pomLicense.setName("The Apache Software License, Version 2.0");
        pomLicense.setUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");
        pomLicense.setDistribution("repo");
        pom.setLicenses(Collections.singletonList(pomLicense));

        Assert.assertNotNull(poms.stream().filter(p -> {
            return p.getArtifactId().equals("spock-core")
                && p.getLicenses().get(0).getName().equals("The Apache Software License, Version 2.0");
        }).findFirst().orElse(null));
    }
}
