package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;

class Setting {

    private File localRepositoryPath;

    public File getLocalRepositoryPath() {
        return localRepositoryPath;
    }

    public void setLocalRepositoryPath(File localRepositoryPath) {
        this.localRepositoryPath = localRepositoryPath;
    }

    public void setLocalRepositoryPath(String localRepositoryPath) {
        this.localRepositoryPath = new File(localRepositoryPath);
    }
}
