package at.porscheinformatik.sonarqube.licensecheck.license;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;

import org.apache.commons.lang3.StringUtils;

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

        if (serializedLicensesString != null && serializedLicensesString.startsWith("["))
        {
            JsonReader jsonReader = Json.createReader(new StringReader(serializedLicensesString));
            JsonArray licensesJson = jsonReader.readArray();
            for (int i = 0; i < licensesJson.size(); i++)
            {
                JsonObject licenseJson = licensesJson.getJsonObject(i);
                licenses.add(new License(licenseJson.getString("name"), licenseJson.getString("identifier"),
                    licenseJson.getString("status")));
            }
        }
        else
        {
            // deprecated - remove with later release
            if (StringUtils.isNotEmpty(serializedLicensesString))
            {
                String[] parts = serializedLicensesString.split(";");

                for (String licenseString : parts)
                {
                    String[] subParts = licenseString.split("~");
                    String name = subParts.length > 0 ? subParts[0] : null;
                    String identifier = subParts.length > 1 ? subParts[1] : null;
                    String status = subParts.length > 2 ? subParts[2] : null;
                    licenses.add(new License(name, identifier, status));
                }
            }
        }
        return licenses;
    }

    public static String createString(Collection<License> licenses)
    {
        TreeSet<License> licenseSet = new TreeSet<>();
        licenseSet.addAll(licenses);

        StringWriter jsonString = new StringWriter();
        JsonGenerator generator = Json.createGenerator(jsonString);
        generator.writeStartArray();
        for (License license : licenseSet)
        {
            generator.writeStartObject();
            generator.write("name", license.getName());
            generator.write("identifier", license.getIdentifier());
            generator.write("status", license.getStatus());
            generator.writeEnd();
        }
        generator.writeEnd();
        generator.close();

        return jsonString.toString();
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
