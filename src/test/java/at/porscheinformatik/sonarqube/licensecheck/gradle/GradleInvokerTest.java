package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class GradleInvokerTest {

    private static String root;

    @Before
    public void setup() {
        root = new File(this.getClass().getClassLoader().getResource("gradle/build.gradle").getFile()).getParent();
    }

    @Test
    public void invokeTasks() throws Exception {
        GradleInvoker gradleInvoker = new GradleInvoker(root);

        Assert.assertEquals(true, gradleInvoker.invoke("tasks").contains("build"));
    }

    @Test
    public void invokeDependenciesBare() throws Exception {
        GradleInvoker gradleInvoker = new GradleInvoker(root);

        Assert.assertEquals(gradleInvoker.invoke("dependencies").contains("groovy"), true);
    }
}
