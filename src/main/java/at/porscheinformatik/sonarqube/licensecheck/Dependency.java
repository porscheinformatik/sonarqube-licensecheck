package at.porscheinformatik.sonarqube.licensecheck;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;

public class Dependency implements Comparable<Dependency>
{
    private String name;
    private String version;
    private String license;
    private String status;
    private String pomPath;

    public Dependency(String name, String version, String license)
    {
        super();
        this.name = name;
        this.version = version;
        this.license = license;
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

    public void setStatus(final String status)
    {
        this.status = status;
    }

    public String getStatus()
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

    public static List<Dependency> fromString(String serializedDependencyString)
    {
        List<Dependency> dependencies = new ArrayList<>();

        if (serializedDependencyString != null)
        {
            if (serializedDependencyString.startsWith("["))
            {
                try (JsonReader jsonReader = Json.createReader(new StringReader(serializedDependencyString)))
                {
                    JsonArray dependenciesJson = jsonReader.readArray();
                    for (int i = 0; i < dependenciesJson.size(); i++)
                    {
                        JsonObject dependencyJson = dependenciesJson.getJsonObject(i);
                        dependencies.add(
                            new Dependency(dependencyJson.getString("name"), dependencyJson.getString("version"),
                                dependencyJson.getString("license")));
                    }
                }
            }
            else
            {
                // deprecated - remove with later release
                String[] parts = serializedDependencyString.split(";");

                for (String dependencyString : parts)
                {
                    String[] subParts = dependencyString.split("~");
                    String name = subParts.length > 0 ? subParts[0] : null;
                    String version = subParts.length > 1 ? subParts[1] : null;
                    String license = subParts.length > 2 ? subParts[2] : null;
                    dependencies.add(new Dependency(name, version, license));
                }
            }
        }

        return dependencies;
    }

    public static String createString(Collection<Dependency> dependencies)
    {
        TreeSet<Dependency> sortedDependencies = new TreeSet<>();
        sortedDependencies.addAll(dependencies);

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
            generator.writeEnd();
        }
        generator.writeEnd();
        generator.close();
        return jsonString.toString();
    }
}
