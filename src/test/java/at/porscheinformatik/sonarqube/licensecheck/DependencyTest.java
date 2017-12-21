package at.porscheinformatik.sonarqube.licensecheck;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

public class DependencyTest
{
    private static final String DEPENDENCIES_JSON =
        "[{\"name\":\"another\",\"version\":\"2.1.0\",\"license\":\"MIT\"}," +
            "{\"name\":\"library\",\"version\":\"1.0.0\",\"license\":\"Apache-2.0\"}]";
    private static final String DEPENDENCIES_STRING = "another~2.1.0~MIT;library~1.0.0~Apache-2.0";
    private static final Dependency DEP1 = new Dependency("another", "2.1.0", "MIT");
    private static final Dependency DEP2 = new Dependency("library", "1.0.0", "Apache-2.0");

    @Test
    public void createString()
    {
        String dependenciesJson = Dependency.createString(asList(DEP2, DEP1));

        assertThat(dependenciesJson, equalTo(DEPENDENCIES_JSON));
    }

    @Test
    public void fromStringOld()
    {
        List<Dependency> dependencies = Dependency.fromString(DEPENDENCIES_STRING);

        assertThat(dependencies, hasItems(DEP1, DEP2));
    }

    @Test
    public void fromStringNew()
    {
        List<Dependency> dependencies = Dependency.fromString(DEPENDENCIES_JSON);

        assertThat(dependencies, hasItems(DEP1, DEP2));
    }
}
