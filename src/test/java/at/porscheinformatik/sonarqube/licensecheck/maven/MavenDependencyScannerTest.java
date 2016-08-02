package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;

public class MavenDependencyScannerTest
{
    @Test
    public void testLicensesAreFound()
    {
        //		String mavenProjectDependencies = "[{\"k\":\"commons-lang:commons-lang\",\"v\":\"2.6\",\"s\":\"compile\",\"d\":[]},{\"k\":\"commons-codec:commons-codec\",\"v\":\"1.10\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.springframework:spring-context\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[{\"k\":\"org.springframework:spring-aop\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[{\"k\":\"aopalliance:aopalliance\",\"v\":\"1.0\",\"s\":\"compile\",\"d\":[]}]},{\"k\":\"org.springframework:spring-beans\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.springframework:spring-core\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.springframework:spring-expression\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[]}]},{\"k\":\"org.liquibase:liquibase-core\",\"v\":\"3.4.2\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.slf4j:slf4j-api\",\"v\":\"1.7.21\",\"s\":\"compile\",\"d\":[]},{\"k\":\"com.google.code.findbugs:annotations\",\"v\":\"2.0.0\",\"s\":\"compile\",\"d\":[]},{\"k\":\"com.google.code.findbugs:jsr305\",\"v\":\"2.0.0\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.testng:testng\",\"v\":\"6.8.21\",\"s\":\"test\",\"d\":[{\"k\":\"org.beanshell:bsh\",\"v\":\"2.0b4\",\"s\":\"test\",\"d\":[]},{\"k\":\"com.beust:jcommander\",\"v\":\"1.27\",\"s\":\"test\",\"d\":[]}]},{\"k\":\"ch.qos.logback:logback-classic\",\"v\":\"1.1.7\",\"s\":\"test\",\"d\":[{\"k\":\"ch.qos.logback:logback-core\",\"v\":\"1.1.7\",\"s\":\"test\",\"d\":[]}]},{\"k\":\"org.slf4j:jcl-over-slf4j\",\"v\":\"1.7.21\",\"s\":\"test\",\"d\":[]},{\"k\":\"org.mockito:mockito-core\",\"v\":\"1.10.19\",\"s\":\"test\",\"d\":[{\"k\":\"org.hamcrest:hamcrest-core\",\"v\":\"1.3\",\"s\":\"test\",\"d\":[]},{\"k\":\"org.objenesis:objenesis\",\"v\":\"2.1\",\"s\":\"test\",\"d\":[]}]}]";

        String mavenProjectDependencies =
            "[{\"k\":\"commons-lang:commons-lang\",\"v\":\"2.6\",\"s\":\"compile\",\"d\":[]},{\"k\":\"commons-codec:commons-codec\",\"v\":\"1.10\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.springframework:spring-context\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[{\"k\":\"org.springframework:spring-aop\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[{\"k\":\"aopalliance:aopalliance\",\"v\":\"1.0\",\"s\":\"compile\",\"d\":[]}]},{\"k\":\"org.springframework:spring-beans\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.springframework:spring-core\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.springframework:spring-expression\",\"v\":\"4.2.6.RELEASE\",\"s\":\"compile\",\"d\":[]}]},{\"k\":\"org.liquibase:liquibase-core\",\"v\":\"3.4.2\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.slf4j:slf4j-api\",\"v\":\"1.7.21\",\"s\":\"compile\",\"d\":[]},{\"k\":\"com.google.code.findbugs:annotations\",\"v\":\"2.0.0\",\"s\":\"compile\",\"d\":[]},{\"k\":\"com.google.code.findbugs:jsr305\",\"v\":\"2.0.0\",\"s\":\"compile\",\"d\":[]},{\"k\":\"org.testng:testng\",\"v\":\"6.8.21\",\"s\":\"test\",\"d\":[{\"k\":\"org.beanshell:bsh\",\"v\":\"2.0b4\",\"s\":\"test\",\"d\":[]},{\"k\":\"com.beust:jcommander\",\"v\":\"1.27\",\"s\":\"test\",\"d\":[]}]},{\"k\":\"ch.qos.logback:logback-classic\",\"v\":\"1.1.7\",\"s\":\"test\",\"d\":[{\"k\":\"ch.qos.logback:logback-core\",\"v\":\"1.1.7\",\"s\":\"test\",\"d\":[]}]},{\"k\":\"org.slf4j:jcl-over-slf4j\",\"v\":\"1.7.21\",\"s\":\"test\",\"d\":[]},{\"k\":\"org.mockito:mockito-core\",\"v\":\"1.10.19\",\"s\":\"test\",\"d\":[{\"k\":\"org.hamcrest:hamcrest-core\",\"v\":\"1.3\",\"s\":\"test\",\"d\":[]},{\"k\":\"org.objenesis:objenesis\",\"v\":\"2.1\",\"s\":\"test\",\"d\":[]}]}]";

        File moduleDir = new File(".");

        Map<Pattern, String> licenseMap = new HashMap<>();
        licenseMap.put(Pattern.compile("Indiana University.*"), "UNI");
        licenseMap.put(Pattern.compile(".*BSD.*"), "BSD-3-Clause");
        MavenLicenseService licenseService = Mockito.mock(MavenLicenseService.class);
        Mockito.when(licenseService.getLicenseMap()).thenReturn(licenseMap);
        final MavenDependencyService dependencyService = Mockito.mock(MavenDependencyService.class);
        Scanner scanner = new MavenDependencyScanner(licenseService, dependencyService);

        // -
        List<Dependency> dependencies = scanner.scan(moduleDir, mavenProjectDependencies);

        // -
        for (Dependency dep : dependencies)
        {
            if ("org.codehaus.staxmate:staxmate".equals(dep.getName()))
            {
                Assert.assertThat(dep.getLicense(), CoreMatchers.is("BSD-3-Clause"));
            }
            else if ("xpp3:xpp3".equals(dep.getName()) && "1.1.4c".equals(dep.getVersion()))
            {
                Assert.assertThat(dep.getLicense(), CoreMatchers.is("UNI"));
            }
        }
    }
}
