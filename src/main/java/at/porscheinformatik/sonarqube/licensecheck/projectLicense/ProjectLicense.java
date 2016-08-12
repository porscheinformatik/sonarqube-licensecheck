package at.porscheinformatik.sonarqube.licensecheck.projectLicense;

public class ProjectLicense implements Comparable<ProjectLicense>
{
    private String license;
    private String projectName;
    private String status;
    private String projectKey;

    public ProjectLicense(String license, String projectName, String status, String projectKey)
    {
        super();
        this.license = license;
        this.projectName = projectName;
        this.status = status;
        this.projectKey = projectKey;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public int compareTo(ProjectLicense o)
    {
        if (o == null)
        {
            return 1;
        }
        else if (this.getProjectName().compareTo(o.getProjectName()) == 0)
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
            return this.getProjectName().compareTo(o.getProjectName());
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

        ProjectLicense projectLicense = (ProjectLicense) object;

        if (projectLicense.getLicense().equals(this.getLicense())
            && projectLicense.getProjectName().equals(this.getProjectName()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((getLicense() == null) ? 0 : getLicense().hashCode());
        result = (prime * result) + ((getProjectName() == null) ? 0 : getProjectName().hashCode());
        result = (prime * result) + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }
}
