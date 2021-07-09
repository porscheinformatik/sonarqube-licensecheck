package at.porscheinformatik.sonarqube.licensecheck.licensemapping;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.codehaus.plexus.util.StringUtils;

public class LicenseMapping implements Comparable<LicenseMapping>
{
    public static final String FIELD_LICENSE = "license";
    public static final String FIELD_REGEX = "regex";

    private final Pattern regex;
    private final String license;

    public LicenseMapping(String regex, String license)
    {
        super();
        this.regex = Pattern.compile(regex);
        this.license = license;
    }

    public Pattern getRegex()
    {
        return regex;
    }

    public String getLicense()
    {
        return license;
    }

    @Override
    public int compareTo(LicenseMapping o)
    {
        if (o == null)
        {
            return 1;
        }
        else if (this.license.compareTo(o.license) == 0)
        {
            return this.regex.toString().compareTo(o.regex.toString());
        }
        else
        {
            return this.license.compareTo(o.license);
        }
    }

    public static List<LicenseMapping> fromString(String licenseMappingString)
    {
        List<LicenseMapping> licensMappings = new ArrayList<>();

        if (licenseMappingString != null && licenseMappingString.startsWith("["))
        {
            try (JsonReader jsonReader = Json.createReader(new StringReader(licenseMappingString)))
            {
                JsonArray licensesJson = jsonReader.readArray();
                for (int i = 0; i < licensesJson.size(); i++)
                {
                    JsonObject licenseJson = licensesJson.getJsonObject(i);
                    String regex;
                    try
                    {
                        regex = licenseJson.getString(FIELD_REGEX);
                    }
                    catch (NullPointerException e)
                    {
                        regex = licenseJson.getString("licenseNameRegEx", null);
                    }

                    if (regex != null)
                    {
                        licensMappings.add(new LicenseMapping(regex, licenseJson.getString(FIELD_LICENSE)));
                    }
                }
            }
        }
        else if (StringUtils.isNotEmpty(licenseMappingString))
        {
            // deprecated - remove with later release
            String[] licenseMappingEntries = licenseMappingString.split(";");
            for (String licenseMappingEntry : licenseMappingEntries)
            {
                String[] licenseMappingEntryParts = licenseMappingEntry.split("~");
                licensMappings.add(new LicenseMapping(licenseMappingEntryParts[0], licenseMappingEntryParts[1]));
            }
        }

        return licensMappings;
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
        LicenseMapping that = (LicenseMapping) o;
        return Objects.equals(regex.pattern(), that.regex.pattern()) &&
            Objects.equals(license, that.license);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(regex, license);
    }
}
