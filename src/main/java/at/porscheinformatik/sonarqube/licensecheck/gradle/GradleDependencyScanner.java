package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.gradle.license.LicenseMatcher;
import at.porscheinformatik.sonarqube.licensecheck.gradle.license.LicenseResolver;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import at.porscheinformatik.sonarqube.licensecheck.mavendependency.MavenDependencyService;
import at.porscheinformatik.sonarqube.licensecheck.mavenlicense.MavenLicenseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GradleDependencyScanner implements Scanner {
    private final LicenseMatcher licenseMatcher;
    private final LicenseResolver licenseResolver;
    private final PomDependencyMapper pomDependencyMapper;

    private File projectRoot;

    public GradleDependencyScanner(MavenLicenseService mavenLicenseService, MavenDependencyService mavenDependencyService) {
        this.licenseMatcher = new LicenseMatcher(mavenLicenseService);
        this.licenseResolver = new LicenseResolver(mavenDependencyService);
        this.pomDependencyMapper = new PomDependencyMapper(licenseMatcher);
    }

    @Override
    public List<Dependency> scan(File moduleDir) {
        this.projectRoot = moduleDir;

        try {
            return resolveDependenciesWithLicenses();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Dependency> resolveDependenciesWithLicenses() throws Exception {
        GradlePomResolver gradlePomResolver = new GradlePomResolver(projectRoot);
        List<Model> poms = gradlePomResolver.resolvePomsOfAllDependencies();
        List<Dependency> dependencies = pomsToDependencies(poms);

        return dependencies.stream()
            .map(this::resolveLicenseByPackage)
            .map(this::matchLicenseByMap)
            .collect(Collectors.toList());
    }

    private List<Dependency> pomsToDependencies(List<Model> poms) {
        return poms.stream()
            .map(pomDependencyMapper::toDependency)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Dependency resolveLicenseByPackage(Dependency dependency) {
        // todo: blank check on another level?
        if (StringUtils.isBlank(dependency.getLicense())) {
            dependency.setLicense(licenseResolver.byPackage(dependency.getName()));
        }
        return dependency;
    }

    private Dependency matchLicenseByMap(Dependency dependency) {
        dependency.setLicense(licenseMatcher.viaLicenseMap(dependency.getLicense()));
        return dependency;
    }
}
