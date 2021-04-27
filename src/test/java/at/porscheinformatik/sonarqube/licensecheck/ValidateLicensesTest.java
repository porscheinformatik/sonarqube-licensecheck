package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.fs.internal.DefaultInputProject;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorStorage;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue;
import org.sonar.api.scanner.fs.InputProject;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

public class ValidateLicensesTest
{
    private static final License APACHE_LICENSE = new License("Apache-2.0", "Apache-2.0", "true");
    private ValidateLicenses validateLicenses;
    private ProjectDefinition projectDefinition;
    private InputProject module;

    @Before
    public void setup()
    {
        module = mock(InputProject.class);
        when(module.key()).thenReturn("at.porscheinformatik.demo:demo");
        final LicenseService licenseService = mock(LicenseService.class);
        when(licenseService.getLicenses(module)).thenReturn(Arrays.asList(new License("MIT", "MIT", "false"),
            new License("LGPL is fantastic", "LGPL", "true"), APACHE_LICENSE));
        validateLicenses = new ValidateLicenses(licenseService);
    }

    private SensorContext createContext()
    {
        SensorContext context = mock(SensorContext.class);
        DefaultInputModule inputModule = mock(DefaultInputModule.class);
        when(context.project()).thenReturn(module);
        when(inputModule.definition()).thenReturn(projectDefinition);
        when(context.module()).thenReturn(inputModule);
        return context;
    }

    @Test
    public void licenseNotAllowed()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "MIT")), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void licenseAllowed()
    {
        SensorContext context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")),
            context);

        verify(context, never()).newIssue();
    }

    //  (LGPL OR Apache-2.0) AND (LGPL OR Apache-2.0)    
    @Test
    public void checkSpdxOrCombination()
    {
        SensorContext context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("another", "2.0", "(LGPL OR Apache-2.0)"),
            new Dependency("thing", "1.0", "(MIT OR Apache-2.0)")), context);

        verify(context, never()).newIssue();
    }

    @Test
    public void checkSpdxSeveralOrCombination()
    {
        SensorContext context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "(Apache-2.0 OR MIT OR Apache-2.0 OR LGPL)")), context);

        verify(context, never()).newIssue();
    }

    @Test
    public void checkSpdxAndCombination()
    {
        SensorContext context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(LGPL AND Apache-2.0)")), context);

        verify(context, never()).newIssue();
    }

    @Test
    public void checkSpdxAndCombinationNotAllowed()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(
            deps(new Dependency("another", "2.0", "LGPL"), new Dependency("thing", "1.0", "(Apache-2.0 AND MIT)")),
            context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void checkSpdxAndCombinationNotFound()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(Apache-2.0 AND Apache-1.1)")), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    //  LGPL OR Apache-2.0 AND MIT
    @Test
    public void checkSpdxOrAndCombination()
    {
        SensorContext context = createContext();

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "(LGPL OR (Apache-2.0 AND MIT))")),
            context);

        verify(context, never()).newIssue();
    }

    @Test
    public void licenseNull()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", null)), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseUnknown()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "Mamamia")), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseMissingWithCustomMapping()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        Map<String, String> customLicenseMapping = new ConcurrentHashMap<>();
        customLicenseMapping.put("thing", "MIT");

        Dependency testDependency = validateLicenses
                .validateLicenses(deps(new Dependency("thing", "1.0", "")), context, customLicenseMapping, null)
                .stream().findFirst().get();
                
        verify(context).newIssue();
        assertTrue("Dependency should have MIT license according to mapping",
                "MIT".equals(testDependency.getLicense()));
        assertFalse("Issue should not contain the unlisted license key",
                issue.toString().contains(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseUnknownWithCustomMapping()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        Map<String, String> customLicenseMapping = new ConcurrentHashMap<>();
        customLicenseMapping.put("thing", "MIT");

        Dependency testDependency = validateLicenses
                .validateLicenses(deps(new Dependency("thing", "1.0", "Some unknown and wrong license")), context,
                        customLicenseMapping, null)
                .stream().findFirst().get();

        verify(context).newIssue();
        // Custom mapping should be ignored in this case
        assertTrue("Dependency should have wrong license according to how it was passed in",
                "Some unknown and wrong license".equals(testDependency.getLicense()));
        assertTrue("Issue should contain the unlisted license key",
                issue.toString().contains(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseKnownWithCustomMapping()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        Map<String, String> customLicenseMapping = new ConcurrentHashMap<>();
        customLicenseMapping.put("thing", "MIT");

        Dependency testDependency = validateLicenses
                .validateLicenses(deps(new Dependency("thing", "1.0", "Apache-2.0")), context, customLicenseMapping,
                        null)
                .stream().findFirst().get();
        // Mapping should be ignored in this case
        assertTrue("Dependency should have Apache-2.0 license according to how it was passed in",
                "Apache-2.0".equals(testDependency.getLicense()));
    }

    @Test
    public void licenseUnknownWithForcedMapping()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        Map<String, String> forcedLicenseMapping = new ConcurrentHashMap<>();
        forcedLicenseMapping.put("thing", "MIT");

        Dependency testDependency = validateLicenses
                .validateLicenses(deps(new Dependency("thing", "1.0", "Some unknown and wrong license")), context, null,
                        forcedLicenseMapping)
                .stream().findFirst().get();

        verify(context).newIssue();
        assertTrue("Dependency should have MIT license according to mapping",
                "MIT".equals(testDependency.getLicense()));
        assertFalse("Issue should not contain the unlisted license key",
                issue.toString().contains(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseKnownWithForcedMapping() 
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        Map<String, String> forcedLicenseMapping = new ConcurrentHashMap<>();
        forcedLicenseMapping.put("thing", "MIT");

        Dependency testDependency = validateLicenses
                .validateLicenses(deps(new Dependency("thing", "1.0", "Apache-2.0")), context, null,
                        forcedLicenseMapping)
                .stream().findFirst().get();
        // Mapping should not be ignored, as it is forced
        assertTrue("Dependency should have MIT license according to mapping",
                "MIT".equals(testDependency.getLicense()));
    }

    @Test
    public void getUsedLicenses()
    {
        assertThat(validateLicenses.getUsedLicenses(deps(), module).size(), is(0));

        Set<License> usedLicensesApache = validateLicenses.getUsedLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")),
            module);

        assertThat(usedLicensesApache.size(), is(1));
        assertThat(usedLicensesApache, CoreMatchers.hasItem(APACHE_LICENSE));
    }

    private static Set<Dependency> deps(Dependency... dependencies)
    {
        final Set<Dependency> dependencySet = new HashSet<>();
        Collections.addAll(dependencySet, dependencies);
        return dependencySet;
    }
}
