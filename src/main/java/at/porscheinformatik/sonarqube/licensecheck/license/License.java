package at.porscheinformatik.sonarqube.licensecheck.license;

import org.codehaus.plexus.util.StringUtils;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class License implements Comparable<License>
{
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";
    public static final String FIELD_ALLOWED = "allowed";
    public static final String STATUS = "status";

    private String name;
    private String identifier;
    private Boolean allowed;

    public License(String name, String identifier, Boolean allowed)
    {
        super();
        this.name = name;
        this.identifier = identifier;
        this.allowed = Objects.requireNonNull(allowed);
    }

    public License(String name, String identifier, String allowed)
    {
        this(name, identifier, Boolean.parseBoolean(allowed));
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

    public Boolean getAllowed()
    {
        return allowed;
    }

    public void setAllowed(Boolean allowed)
    {
        this.allowed = allowed;
    }

    @Override
    public String toString()
    {
        return "{name:" + name + ", identifier:" + identifier + ", status:" + allowed + "}";
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
                        licenseJson.getString(STATUS)));
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
     * @param serializedLicensesString setting string
     * @return a list with licences
     * @deprecated remove with later release
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
     * @param serializedLicensesString setting string
     * @return a list with licences
     * @deprecated remove with later release
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
                    value.getString(STATUS)));
            }
        }

        return licenses;
    }

    public static String createJsonString(Collection<License> licenses)
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
            generator.write(STATUS, license.getAllowed().toString());
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
                return this.allowed.compareTo(o.allowed);
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
            Objects.equals(allowed, license.allowed);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, identifier, allowed);
    }
}
