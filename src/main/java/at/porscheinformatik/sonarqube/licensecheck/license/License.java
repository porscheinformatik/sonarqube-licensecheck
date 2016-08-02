package at.porscheinformatik.sonarqube.licensecheck.license;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class License implements Comparable<License>
{
    private String name;
    private String identifier;
    private String status;

    public License(String name, String identifier, String status)
    {
        super();
        this.name = name;
        this.identifier = identifier;
        this.status = status;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((identifier == null) ? 0 : identifier.hashCode());
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return "{name:" + name + ", identifier:" + identifier + ", status:" + status + "}";
    }

    public static List<License> fromString(String serializedLicensesString)
    {
        List<License> licenses = new ArrayList<>();
        String[] parts = serializedLicensesString.split(";");

        for (int i = 0; i < parts.length; i++)
        {
            String licenseString = parts[i];
            String[] subParts = licenseString.split("~");
            String name = subParts.length > 0 ? subParts[0] : null;
            String identifier = subParts.length > 1 ? subParts[1] : null;
            String status = subParts.length > 2 ? subParts[2] : null;
            licenses.add(new License(name, identifier, status));
        }
        return licenses;
    }

    public static String createString(Collection<License> licenses)
    {
        TreeSet<License> licenseSet = new TreeSet<>();
        licenseSet.addAll(licenses);

        StringBuilder returnString = new StringBuilder();
        for (License license : licenseSet)
        {
            returnString.append(license.getName()).append("~");
            returnString.append(license.getIdentifier()).append("~");
            returnString.append(license.getStatus()).append(";");
        }
        return returnString.toString();
    }

    @Override
    public int compareTo(License o)
    {
        if (o == null)
        {
            return 1;
        }
        else if (this.identifier.compareTo(o.identifier) == 0)
        {
            if (this.name.compareTo(o.name) == 0)
            {
                return this.status.compareTo(o.status);
            }
            else
            {
                return this.name.compareTo(o.name);
            }
        }
        else
        {
            return this.identifier.compareTo(o.identifier);
        }
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (object == null)
        {
            return false;
        }
        if (getClass() != object.getClass())
        {
            return false;
        }

        License license = (License) object;
        if (license.identifier.equals(this.identifier) && license.name.equals(this.name))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
