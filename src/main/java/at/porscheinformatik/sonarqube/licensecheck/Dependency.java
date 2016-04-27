package at.porscheinformatik.sonarqube.licensecheck;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Dependency implements Comparable<Dependency>
{
    private String name;
    private String version;
    private String license;

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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((license == null) ? 0 : license.hashCode());
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Dependency other = (Dependency) obj;
        if (license == null)
        {
            if (other.license != null)
            {
                return false;
            }
        }
        else if (!license.equals(other.license))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        if (version == null)
        {
            if (other.version != null)
            {
                return false;
            }
        }
        else if (!version.equals(other.version))
        {
            return false;
        }
        return true;
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
        String[] parts = serializedDependencyString.split(";");

        for (int i = 0; i < parts.length; i++)
        {
            String dependencyString = parts[i];
            String[] subParts = dependencyString.split("~");
            dependencies.add(new Dependency(subParts[0], subParts[1], subParts[2]));
        }
        return dependencies;
    }

    public static String createString(List<Dependency> dependencies)
    {
        TreeSet<Dependency> dependencySet = new TreeSet<>();
        dependencySet.addAll(dependencies);

        StringBuilder returnString = new StringBuilder();
        for (Dependency dependency : dependencySet)
        {
            returnString.append(dependency.getName()).append("~");
            returnString.append(dependency.getVersion()).append("~");
            returnString.append(dependency.getLicense()).append(";");
        }
        return returnString.toString();
    }
}
