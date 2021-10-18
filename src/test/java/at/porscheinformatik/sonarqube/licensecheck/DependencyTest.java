package at.porscheinformatik.sonarqube.licensecheck;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DependencyTest
{
    private static final String DEPENDENCIES_JSON =
        "[{\"name\":\"another\",\"version\":\"2.1.0\",\"license\":\"MIT\",\"lang\":\"java\"}," +
            "{\"name\":\"library\",\"version\":\"1.0.0\",\"license\":\"Apache-2.0\",\"lang\":\"java\"}]";
    private static final Dependency DEP1 = new Dependency("another", "2.1.0", "MIT");
    private static final Dependency DEP2 = new Dependency("library", "1.0.0", "Apache-2.0");

    @Test
    public void createString()
    {
        String dependenciesJson = Dependency.createString(asList(DEP2, DEP1));

        assertThat(dependenciesJson, equalTo(DEPENDENCIES_JSON));
    }
}
