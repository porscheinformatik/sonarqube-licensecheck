package at.porscheinformatik.sonarqube.licensecheck;

/**
 * A language enum definition, in order to handle multiple languages & repositories
 */
public enum LicenseCheckLanguage {
    DART("dart"),
    JAVA("java"),
    JAVASCRIPT("javascript"),
    SWIFT("swift"),
    PYTHON("python");

    private String language;

    LicenseCheckLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language.toLowerCase();
    }

    @Override
    public String toString() {
        return language;
    }
}
