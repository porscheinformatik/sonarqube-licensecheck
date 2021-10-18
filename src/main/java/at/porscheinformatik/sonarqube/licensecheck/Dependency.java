package at.porscheinformatik.sonarqube.licensecheck;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckRulesDefinition.LANG_JAVA;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.TextRange;

public class Dependency implements Comparable<Dependency>
{
    private String name;
    private String version;
    private String license;
    private String lang;
    private Status status;
    private String pomPath;
    private InputComponent inputComponent;
    private TextRange textRange;

    public Dependency(String name, String version, String license, String lang)
    {
        super();
        this.name = name;
        this.version = version;
        this.license = license;
        this.lang = lang;
    }

    public Dependency(String name, String version, String license)
    {
        this(name, version, license, LANG_JAVA);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public String getLang()
    {
        return lang;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
    }

    public void setStatus(final Status status)
    {
        this.status = status;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getPomPath()
    {
        return pomPath;
    }

    public void setPomPath(String pomPath)
    {
        this.pomPath = pomPath;
    }

    public InputComponent getInputComponent()
    {
        return inputComponent;
    }

    public void setInputComponent(InputComponent inputComponent)
    {
        this.inputComponent = inputComponent;
    }

    public TextRange getTextRange()
    {
        return textRange;
    }

    public void setTextRange(TextRange textRange)
    {
        this.textRange = textRange;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Dependency that = (Dependency) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(version, that.version) &&
            Objects.equals(license, that.license);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, version, license);
    }

    @Override
    public String toString()
    {
        return "{name:" + name + ", version:" + version + ", license:" + license + "}";
    }

    @Override
    public int compareTo(Dependency o)
    {
        if ((o == null) || (o.name == null))
        {
            return 1;
        }
        else if (this.name == null)
        {
            return -1;
        }

        return this.name.compareTo(o.name);
    }

    public static String createString(Collection<Dependency> dependencies)
    {
        TreeSet<Dependency> sortedDependencies = new TreeSet<>(dependencies);

        StringWriter jsonString = new StringWriter();
        JsonGenerator generator = Json.createGenerator(jsonString);
        generator.writeStartArray();
        for (Dependency dependency : sortedDependencies)
        {
            String license = dependency.getLicense();
            generator.writeStartObject();
            generator.write("name", dependency.getName());
            generator.write("version", dependency.getVersion());
            generator.write("license", license != null ? license : " ");
            generator.write("lang", dependency.getLang());
            generator.writeEnd();
        }
        generator.writeEnd();
        generator.close();
        return jsonString.toString();
    }

    public enum Status
    {
        Allowed,
        Forbidden,
        Unknown
    }
}
