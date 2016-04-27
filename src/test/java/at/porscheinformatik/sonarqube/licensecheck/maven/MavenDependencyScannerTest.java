package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;

public class MavenDependencyScannerTest
{
    @Test
    public void test()
    {
        String mavenProjectDependencies = "[{\"k\":\"junit:junit\",\"v\":\"3.8.1\",\"s\":\"test\",\"d\":[]},{\"k\":\"org.sonarsource.sonarqube:sonar-plugin-api\",\"v\":\"5.2\",\"s\":\"provided\",\"d\":[{\"k\":\"org.codehaus.woodstox:woodstox-core-lgpl\",\"v\":\"4.4.0\",\"s\":\"provided\",\"d\":[{\"k\":\"javax.xml.stream:stax-api\",\"v\":\"1.0-2\",\"s\":\"provided\",\"d\":[]}]},{\"k\":\"org.codehaus.woodstox:stax2-api\",\"v\":\"3.1.4\",\"s\":\"provided\",\"d\":[]},{\"k\":\"org.codehaus.staxmate:staxmate\",\"v\":\"2.0.1\",\"s\":\"provided\",\"d\":[]}]},{\"k\":\"org.owasp:dependency-check-core\",\"v\":\"1.3.3\",\"s\":\"compile\",\"d\":[{\"k\":\"org.slf4j:slf4j-api\",\"v\":\"1.7.13\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.owasp:dependency-check-utils\",\"v\":\"1.3.3\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.apache.commons:commons-compress\",\"v\":\"1.10\",\"s\":\"compile\",\"d\":[]},{\"k\":\"commons-io:commons-io\",\"v\":\"2.4\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.apache.commons:commons-lang3\",\"v\":\"3.4\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.apache.lucene:lucene-core\",\"v\":\"4.7.2\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.apache.lucene:lucene-analyzers-common\",\"v\":\"4.7.2\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.apache.lucene:lucene-queryparser\",\"v\":\"4.7.2\",\"s\":\"compile\",\"d\":[{\"k\":\"org.apache.lucene:lucene-queries\",\"v\":\"4.7.2\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.apache.lucene:lucene-sandbox\",\"v\":\"4.7.2\",\"s\":\"compile\",\"d\":[]}]},{\"k\":\"org.apache.velocity:velocity\",\"v\":\"1.7\",\"s\":\"compile\",\"d\":[{\"k\":\"commons-collections:commons-collections\",\"v\":\"3.2.1\",\"s\":\"compile\",\"d\":[]},{\"k\":\"commons-lang:commons-lang\",\"v\":\"2.4\",\"s\":\"compile\",\"d\":[]}]},{\"k\":\"com.h2database:h2\",\"v\":\"1.3.176\",\"s\":\"runtime\",\"d\":[]},{\"k\":\"org.glassfish:javax.json\",\"v\":\"1.0.4\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.jsoup:jsoup\",\"v\":\"1.8.3\",\"s\":\"compile\",\"d\":[]},{\"k\":\"com.sun.mail:mailapi\",\"v\":\"1.5.4\",\"s\":\"compile\",\"d\":[{\"k\":\"javax.activation:activation\",\"v\":\"1.1\",\"s\":\"compile\",\"d\":[]}]}]}]";
        File moduleDir = new File(".");

//        Scanner scanner = new MavenDependencyScanner();
//        List<Dependency> dependencies = scanner.scan(moduleDir, mavenProjectDependencies);

//        Assert.assertThat(dependencies, CoreMatchers.hasItem(new Dependency("org.owasp:dependency-check-core", "1.3.3", " ")));
    }
}
