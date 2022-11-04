package at.porscheinformatik.sonarqube.licensecheck;

import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMapping;
import at.porscheinformatik.sonarqube.licensecheck.dependencymapping.DependencyMappingService;
import at.porscheinformatik.sonarqube.licensecheck.license.License;
import at.porscheinformatik.sonarqube.licensecheck.license.LicenseService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class ValidateLicensesTest
{
    private static final License APACHE_LICENSE = new License("Apache-2.0", "Apache-2.0", "true");
    private ValidateLicenses validateLicenses;
    private ProjectDefinition projectDefinition;
    private InputProject module;
    private DependencyMappingService dependencyMappingService;

    @Before
    public void setup()
    {
        module = mock(InputProject.class);
        when(module.key()).thenReturn("at.porscheinformatik.demo:demo");
        final LicenseService licenseService = mock(LicenseService.class);
        when(licenseService.getLicenses(module)).thenReturn(Arrays.asList(new License("MIT", "MIT", "false"),
            new License("LGPL is fantastic", "LGPL", "true"), APACHE_LICENSE));
        dependencyMappingService = mock(DependencyMappingService.class);
        validateLicenses = new ValidateLicenses(licenseService, dependencyMappingService);
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

        Dependency thing = new Dependency("thing", "1.0", "MIT");
        thing.setInputComponent(context.project());
        validateLicenses.validateLicenses(deps(thing), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckRulesDefinition.RULE_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void licenseAllowed()
    {
        SensorContext context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0", LicenseCheckRulesDefinition.LANG_JS),
                new Dependency("another", "2.0", "Apache-2.0", LicenseCheckRulesDefinition.LANG_JS)),
            context);

        verify(context, never()).newIssue();
    }

    @Test
    public void licenseAllowed_kotlin()
    {
        SensorContext context = createContext();

        validateLicenses.validateLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0", LicenseCheckRulesDefinition.LANG_KOTLIN),
                new Dependency("another", "2.0", "Apache-2.0", LicenseCheckRulesDefinition.LANG_KOTLIN)),
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

        Dependency thing = new Dependency("thing", "1.0", "(Apache-2.0 AND MIT)");
        thing.setInputComponent(context.project());
        validateLicenses.validateLicenses(
            deps(new Dependency("another", "2.0", "LGPL", LicenseCheckRulesDefinition.LANG_JAVA), thing),
            context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckRulesDefinition.RULE_NOT_ALLOWED_LICENSE_KEY));
    }

    @Test
    public void checkSpdxAndCombinationNotFound()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        Dependency thing = new Dependency("thing", "1.0", "(Apache-2.0 AND Apache-1.1)", LicenseCheckRulesDefinition.LANG_KOTLIN);
        thing.setInputComponent(context.project());
        validateLicenses.validateLicenses(deps(thing), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckRulesDefinition.RULE_UNLISTED_KEY));
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

        Dependency thing = new Dependency("thing", "1.0", null, LicenseCheckRulesDefinition.LANG_JS);
        thing.setInputComponent(context.project());
        validateLicenses.validateLicenses(deps(thing), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckRulesDefinition.RULE_UNLISTED_KEY));
    }

    @Test
    public void licenseUnknown()
    {
        SensorContext context = createContext();
        NewIssue issue = new DefaultIssue(mock(DefaultInputProject.class), mock(SensorStorage.class));
        when(context.newIssue()).thenReturn(issue);

        Dependency thing = new Dependency("thing", "1.0", "Mamamia");
        thing.setInputComponent(context.project());
        validateLicenses.validateLicenses(deps(thing), context);

        verify(context).newIssue();
        assertThat(issue.toString(), containsString(LicenseCheckRulesDefinition.RULE_UNLISTED_KEY));
    }

    @Test
    public void getUsedLicenses()
    {
        assertThat(validateLicenses.getUsedLicenses(deps(), module).size(), is(0));

        Set<License> usedLicensesApache = validateLicenses.getUsedLicenses(
            deps(new Dependency("thing", "1.0", "Apache-2.0"), new Dependency("another", "2.0", "Apache-2.0")),
            module);

        assertThat(usedLicensesApache.size(), is(1));
        assertThat(usedLicensesApache, hasItem(APACHE_LICENSE));
    }

    @Test
    public void dependencyMapping()
    {
        when(dependencyMappingService.getDependencyMappings()).thenReturn(
            Collections.singletonList(new DependencyMapping("^thing$", APACHE_LICENSE.getIdentifier(), false)));

        Set<Dependency> deps = validateLicenses.validateLicenses(deps(new Dependency("thing", "1.0", null)), createContext());

        assertThat(deps.size(), is(1));
        assertThat(deps.iterator().next().getLicense(), equalTo(APACHE_LICENSE.getIdentifier()));
    }

    private static Set<Dependency> deps(Dependency... dependencies)
    {
        final Set<Dependency> dependencySet = new HashSet<>();
        Collections.addAll(dependencySet, dependencies);
        return dependencySet;
    }
}
