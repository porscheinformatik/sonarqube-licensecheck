package at.porscheinformatik.sonarqube.licensecheck;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorStorage;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue;
import org.sonar.api.resources.Project;

import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;

public class ValidateLicensesTest
{
    private static final License APACHE_LICENSE = new License("Apache-2.0", "Apache-2.0", "true");
    private ValidateLicenses validateLicenses;
    private Project module;

    @Before
    public void setup()
    {
        module = mock(Project.class);
        final Project root = mock(Project.class);
        when(module.getRoot()).thenReturn(root);
        final LicenseService licenseService = mock(LicenseService.class);
        when(licenseService.getLicenses(root)).thenReturn(Arrays.asList(
            new License("MIT", "MIT", "false"),
            APACHE_LICENSE));
        validateLicenses = new ValidateLicenses(licenseService);
    }

    @Test
    public void licenseNotAllowed()
    {
        final SensorContext context = mock(SensorContext.class);
        DefaultIssue issue = new DefaultIssue(mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "MIT")), module, context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void licenseAllowed()
    {
        final SensorContext context = mock(SensorContext.class);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", "Apache-2.0"),
            new Dependency("another", "2.0", "Apache-2.0")), module, context);

        verify(context, never()).newIssue();
    }

    @Test
    public void licenseNotFound()
    {
        final SensorContext context = mock(SensorContext.class);
        DefaultIssue issue = new DefaultIssue(mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", null)), module, context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckMetrics.LICENSE_CHECK_UNLISTED_KEY));
    }

    @Test
    public void getUsedLicenses()
    {
        assertThat(validateLicenses.getUsedLicenses(deps(), module).size(), is(0));

        Set<License> usedLicensesApache = validateLicenses.getUsedLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"),
                new Dependency("another", "2.0", "Apache-2.0")), module);

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
