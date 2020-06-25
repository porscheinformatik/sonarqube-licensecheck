package at.porscheinformatik.sonarqube.licensecheck.gradle;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GradleLicenseDependency {
    private List<GradleDependency> dependencies = null;
}

@Getter
@Setter
@EqualsAndHashCode
@ToString
class GradleDependency {
    private String moduleName;
    private String moduleUrl;
    private String moduleVersion;
    private String moduleLicense;
    private String moduleLicenseUrl;
}
