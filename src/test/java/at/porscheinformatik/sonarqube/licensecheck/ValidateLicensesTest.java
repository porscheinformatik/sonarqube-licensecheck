package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorStorage;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

public class ValidateLicensesTest
{
    private static final License APACHE_LICENSE = new License("Apache-2.0", "Apache-2.0", "true");
    private ValidateLicenses validateLicenses;
    private ProjectDefinition projectDefinition;

    @Before
    public void setup()
    {
        projectDefinition = mock(ProjectDefinition.class);
        when(projectDefinition.getBaseDir()).thenReturn(new File("."));
        when(projectDefinition.getWorkDir()).thenReturn(new File("."));
        when(projectDefinition.getParent()).thenReturn(projectDefinition);
        final LicenseService licenseService = mock(LicenseService.class);
        when(licenseService.getLicenses(projectDefinition)).thenReturn(Arrays.asList(new License("MIT", "MIT", "false"),
            new License("LGPL is fantastic", "LGPL", "true"), APACHE_LICENSE));
        validateLicenses = new ValidateLicenses(licenseService);
    }

    private SensorContext createContext()
    {
        SensorContext context = mock(SensorContext.class);
        DefaultInputModule inputModule = mock(DefaultInputModule.class);
        when(inputModule.definition()).thenReturn(projectDefinition);
        when(context.module()).thenReturn(inputModule);
        return context;
    }

    @Test
    public void licenseNotAllowed()
    {
        SensorContext context = createContext();
        DefaultIssue issue = new DefaultIssue(mock(SensorStorage.class));
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
        DefaultIssue issue = new DefaultIssue(mock(SensorStorage.class));
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
        DefaultIssue issue = new DefaultIssue(mock(SensorStorage.class));
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
        DefaultIssue issue = new DefaultIssue(mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", null)), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void licenseUnknown()
    {
        SensorContext context = createContext();
        DefaultIssue issue = new DefaultIssue(mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "Mamamia")), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void getUsedLicenses()
    {
        assertThat(validateLicenses.getUsedLicenses(deps(), projectDefinition).size(), is(0));

        Set<License> usedLicensesApache = validateLicenses.getUsedLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")),
            projectDefinition);

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
