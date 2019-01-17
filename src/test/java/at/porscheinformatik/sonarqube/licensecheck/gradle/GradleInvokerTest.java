package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static at.porscheinformatik.sonarqube.licensecheck.gradle.GradleProjectResolver.prepareGradleProject;

public class GradleInvokerTest {

    private static File root;

    @Before
    public void setup() throws IOException {
        root = prepareGradleProject();
    }

    @Test
    public void invokeTasks() throws Exception {
        GradleInvoker gradleInvoker = new GradleInvoker(root.getAbsolutePath());

        Assert.assertTrue(gradleInvoker.invoke("tasks").contains("build"));
    }

    @Test
    public void invokeDependenciesBare() throws Exception {
        GradleInvoker gradleInvoker = new GradleInvoker(root.getAbsolutePath());

        Assert.assertTrue(gradleInvoker.invoke("dependencies").contains("groovy"));
    }
}
