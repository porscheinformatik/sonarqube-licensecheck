package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GradlePomResolverTest {


    private static File projectRoot;

    // todo: mock GradleInvoker

    @Before
    public void setup() throws IOException {
        projectRoot = new File("target/testProject");
        FileUtils.deleteDirectory(projectRoot);
        projectRoot.mkdirs();

        File buildGradleSrc = new File(this.getClass().getClassLoader().getResource("gradle/build.gradle").getFile());
        File buildGradleTrg = new File(projectRoot, "build.gradle");
        FileUtils.copyFile(buildGradleSrc, buildGradleTrg);
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
