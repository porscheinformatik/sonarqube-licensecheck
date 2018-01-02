package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;

import org.apache.commons.lang3.StringUtils;

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

        MavenLicense mavenLicense = (MavenLicense) object;
        return mavenLicense.license.equals(this.license)
            && mavenLicense.regex.toString().equals(this.regex.toString());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((license == null) ? 0 : license.hashCode());
        result = (prime * result) + ((regex == null) ? 0 : regex.hashCode());
        return result;
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
                    String regex = null;
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
        TreeSet<MavenLicense> mavenLicenseSet = new TreeSet<>();
        mavenLicenseSet.addAll(mavenLicenses);

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
}
