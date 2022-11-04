package at.porscheinformatik.sonarqube.licensecheck.projectlicense;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectLicense implements Comparable<ProjectLicense>
{
    public static final String FIELD_PROJECT_KEY = "projectKey";
    public static final String FIELD_LICENSE = "license";
    public static final String FIELD_ALLOWED = "allowed";

    private final String projectKey;
    private final String license;
    private final Boolean allowed;

    public ProjectLicense(String projectKey, String license, Boolean allowed)
    {
        super();
        this.projectKey = projectKey;
        this.license = license;
        this.allowed = allowed;
    }

    public ProjectLicense(String projectKey, String license, String allowed)
    {
        this(projectKey, license, Boolean.parseBoolean(allowed));
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public String getLicense()
    {
        return license;
    }

    public Boolean getAllowed()
    {
        return allowed;
    }

    @Deprecated
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
                    projectLicenseJson.getString(FIELD_PROJECT_KEY),
                    projectLicenseJson.getString(FIELD_LICENSE),
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
                return this.getAllowed().compareTo(o.getAllowed());
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
            Objects.equals(allowed, that.allowed);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(projectKey, license, allowed);
    }
}
