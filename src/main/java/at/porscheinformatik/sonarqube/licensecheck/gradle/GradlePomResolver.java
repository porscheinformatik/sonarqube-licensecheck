package at.porscheinformatik.sonarqube.licensecheck.gradle;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class GradlePomResolver {

    private final File projectRoot;

    GradlePomResolver(File projectRoot) {
        this.projectRoot = projectRoot;
    }

    List<Model> resolvePomsOfAllDependencies() throws Exception {
        File targetDir = resolvePomsAsFiles();
        return parsePomsInDir(targetDir);
    }

    private File resolvePomsAsFiles() throws Exception {
        String relativePoms = "build/poms";

        GradleInvoker gradleInvoker = new GradleInvoker(projectRoot.getAbsolutePath());
        gradleInvoker.invoke("copyPoms", "-I", createInitScript());

        File targetDir = new File(projectRoot, relativePoms);
        assert targetDir.exists();
        return targetDir;
    }

    private List<Model> parsePomsInDir(File targetDir) {
        Collection<File> pomFiles = FileUtils.listFiles(targetDir, new String[]{"pom"}, false);

        return pomFiles.stream()
            .map(File::getAbsolutePath)
            .map(this::parsePom)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Model parsePom(String pomPath) {
        try {
            File file = new File(pomPath);
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            return pomReader.read(new FileInputStream(file));
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createInitScript() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("gradle/pom.gradle");
        File buildDir = new File(projectRoot, "build");
        File file = new File(buildDir, "pom.gradle");
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
