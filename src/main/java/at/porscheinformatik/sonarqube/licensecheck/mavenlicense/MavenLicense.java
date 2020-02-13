package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;

import org.codehaus.plexus.util.StringUtils;

public class MavenLicense implements Comparable<MavenLicense>
{
    private final Pattern regex;
    private final String license;

    public MavenLicense(String regex, String license)
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
    public int compareTo(MavenLicense o)
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

    public static List<MavenLicense> fromString(String mavenLicenseString)
    {
        List<MavenLicense> mavenLicenses = new ArrayList<>();

        if (mavenLicenseString != null && mavenLicenseString.startsWith("["))
        {
            try (JsonReader jsonReader = Json.createReader(new StringReader(mavenLicenseString)))
            {
                JsonArray licensesJson = jsonReader.readArray();
                for (int i = 0; i < licensesJson.size(); i++)
                {
                    JsonObject licenseJson = licensesJson.getJsonObject(i);
                    String regex;
                    try
                    {
                        regex = licenseJson.getString("regex");
                    }
                    catch (NullPointerException e)
                    {
                        regex = licenseJson.getString("licenseNameRegEx", null);
                    }

                    if (regex != null)
                    {
                        mavenLicenses.add(new MavenLicense(regex, licenseJson.getString("license")));
                    }
                }
            }
        }
        else if (StringUtils.isNotEmpty(mavenLicenseString))
        {
            // deprecated - remove with later release
            String[] mavenLicenseEntries = mavenLicenseString.split(";");
            for (String mavenLicenseEntry : mavenLicenseEntries)
            {
                String[] mavenLicenseEntryParts = mavenLicenseEntry.split("~");
                mavenLicenses.add(new MavenLicense(mavenLicenseEntryParts[0], mavenLicenseEntryParts[1]));
            }
        }

        return mavenLicenses;
    }

    public static String createString(Collection<MavenLicense> mavenLicenses)
    {
        TreeSet<MavenLicense> mavenLicenseSet = new TreeSet<>(mavenLicenses);

        StringWriter jsonString = new StringWriter();
        JsonGenerator generator = Json.createGenerator(jsonString);
        generator.writeStartArray();
        for (MavenLicense mavenLicense : mavenLicenseSet)
        {
            generator.writeStartObject();
            generator.write("regex", mavenLicense.getRegex().pattern());
            generator.write("license", mavenLicense.getLicense());
            generator.writeEnd();
        }
        generator.writeEnd();
        generator.close();

        return jsonString.toString();
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
        MavenLicense that = (MavenLicense) o;
        return Objects.equals(regex, that.regex) &&
            Objects.equals(license, that.license);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(regex, license);
    }
}
