package at.porscheinformatik.sonarqube.licensecheck.projectLicense;

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

public class ProjectLicense implements Comparable<ProjectLicense>
{
    private final String projectKey;
    private final String license;
    private final String status;

    public ProjectLicense(String projectKey, String license, String status)
    {
        super();
        this.projectKey = projectKey;
        this.license = license;
        this.status = status;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public String getLicense()
    {
        return license;
    }

    public String getStatus()
    {
        return status;
    }

    public static String createString(Collection<ProjectLicense> projectLicenses)
    {
        TreeSet<ProjectLicense> projectLicenseSet = new TreeSet<>();
        projectLicenseSet.addAll(projectLicenses);

        StringWriter jsonString = new StringWriter();
        JsonGenerator generator = Json.createGenerator(jsonString);
        generator.writeStartArray();
        for (ProjectLicense projectLicense : projectLicenseSet)
        {
            generator.writeStartObject();
            generator.write("projectKey", projectLicense.getProjectKey());
            generator.write("license", projectLicense.getLicense());
            generator.write("status", projectLicense.getStatus());
            generator.writeEnd();
        }
        generator.writeEnd();
        generator.close();

        return jsonString.toString();
    }

    public static List<ProjectLicense> fromString(String projectLicensesString)
    {
        List<ProjectLicense> projectLicenses = new ArrayList<>();

        try (JsonReader jsonReader = Json.createReader(new StringReader(projectLicensesString)))
        {
            JsonArray projectLicensesJson = jsonReader.readArray();
            for (int i = 0; i < projectLicensesJson.size(); i++)
            {
                JsonObject projectLicenseJson = projectLicensesJson.getJsonObject(i);
                projectLicenses.add(new ProjectLicense(
                    projectLicenseJson.getString("projectKey"),
                    projectLicenseJson.getString("license"),
                    projectLicenseJson.getString("status")));
            }
        }

        return projectLicenses;
    }

    public int compareTo(ProjectLicense o)
    {
        if (o == null)
        {
            return 1;
        }

        if (this.getProjectKey().compareTo(o.getProjectKey()) == 0)
        {
            if (this.getLicense().compareTo(o.getLicense()) == 0)
            {
                return this.getStatus().compareTo(o.getStatus());
            }
            else
            {
                return this.getLicense().compareTo(o.getLicense());
            }
        }
        else
        {
            return this.getProjectKey().compareTo(o.getProjectKey());
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
        ProjectLicense that = (ProjectLicense) o;
        return Objects.equals(projectKey, that.projectKey) &&
            Objects.equals(license, that.license) &&
            Objects.equals(status, that.status);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(projectKey, license, status);
    }
}
