package at.porscheinformatik.sonarqube.licensecheck.gradle;

import at.porscheinformatik.sonarqube.licensecheck.Dependency;
import at.porscheinformatik.sonarqube.licensecheck.interfaces.Scanner;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

public class GradleDependencyScanner implements Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleDependencyScanner.class);

    public static void main(String[] args) {
        GradleDependencyScanner s = new GradleDependencyScanner();
        List<Dependency> dependencies = s.scan(new File("C:\\Encode\\code\\license-demo-gradle-java11"));
        dependencies.forEach(System.out::println);
    }

    @Override
    public List<Dependency> scan(File moduleDir) {
        Gson gson = new Gson();
        String filePath =
            moduleDir.getAbsolutePath() + File.separator + "build" + File.separator + "reports" + File.separator + "dependency-license" + File.separator + "license-details.json";
        try {
            GradleLicenseDependency gradleLicenseDependency =
                gson.fromJson(new FileReader(filePath), GradleLicenseDependency.class);
            return convert2Dependencies(gradleLicenseDependency);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    private List<Dependency> convert2Dependencies(GradleLicenseDependency gradleLicenseDependency) {
        List<GradleDependency> gDependencies = gradleLicenseDependency.getDependencies();
        if (gDependencies != null) {
            return gDependencies.stream().map(this::convert2Dependency).collect(Collectors.toList());
        }
        return null;
    }

    private Dependency convert2Dependency(GradleDependency gd) {
        Dependency d = new Dependency(gd.getModuleName(), gd.getModuleVersion(), gd.getModuleLicense());
        d.setPomPath(gd.getModuleLicenseUrl());
        return d;
    }
}
