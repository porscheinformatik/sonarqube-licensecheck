package at.porscheinformatik.sonarqube.licensecheck.license;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.codehaus.plexus.util.StringUtils;

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
    public String toString()
    {
        return "{name:" + name + ", identifier:" + identifier + ", status:" + status + "}";
    }

    public static List<License> fromString(String serializedLicensesString)
    {
        if (serializedLicensesString == null)
        {
            return new ArrayList<>();
        }

        if (serializedLicensesString.startsWith("["))
        {
            List<License> licenses = new ArrayList<>();

            try (JsonReader jsonReader = Json.createReader(new StringReader(serializedLicensesString)))
            {
                JsonArray licensesJson = jsonReader.readArray();
                for (JsonObject licenseJson : licensesJson.getValuesAs(JsonObject.class))
                {
                    licenses.add(new License(licenseJson.getString("name"), licenseJson.getString("identifier"),
                        licenseJson.getString("status")));
                }
            }
            return licenses;
        }
        else if (serializedLicensesString.startsWith("{"))
        {
            return readLegacyJson(serializedLicensesString);
        }
        else
        {
            return readLegacySeparated(serializedLicensesString);
        }
    }

    /**
     * @deprecated remove with later release
     * @param serializedLicensesString setting string
     * @return a list with licences
     */
    @Deprecated
    private static List<License> readLegacySeparated(String serializedLicensesString)
    {
        List<License> licenses = new ArrayList<>();

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

        return licenses;
    }

    /**
     * @deprecated remove with later release
     * @param serializedLicensesString setting string
     * @return a list with licences
     */
    @Deprecated
    private static List<License> readLegacyJson(String serializedLicensesString)
    {
        List<License> licenses = new ArrayList<>();

        try (JsonReader jsonReader = Json.createReader(new StringReader(serializedLicensesString)))
        {
            JsonObject licensesJson = jsonReader.readObject();
            for (Map.Entry<String, JsonValue> licenseJson : licensesJson.entrySet())
            {
                JsonObject value = (JsonObject) licenseJson.getValue();
                licenses.add(new License(value.getString("name"), licenseJson.getKey(),
                    value.getString("status")));
            }
        }

        return licenses;
    }

    public static String createString(Collection<License> licenses)
    {
        TreeSet<License> licenseSet = new TreeSet<>(licenses);

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

        if (this.identifier.compareTo(o.identifier) == 0)
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
        License license = (License) o;
        return Objects.equals(name, license.name) &&
            Objects.equals(identifier, license.identifier) &&
            Objects.equals(status, license.status);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, identifier, status);
    }
}
