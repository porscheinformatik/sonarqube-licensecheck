package at.porscheinformatik.sonarqube.licensecheck.dependency;

public class AllowedDependency
{
    private String key;
    private String license;

    public AllowedDependency(String key, String license)
    {
        super();
        this.key = key;
        this.license = license;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((key == null) ? 0 : key.hashCode());
        result = (prime * result) + ((license == null) ? 0 : license.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        AllowedDependency other = (AllowedDependency) obj;
        if (key == null)
        {
            if (other.key != null)
            {
                return false;
            }
        }
        else if (!key.equals(other.key))
        {
            return false;
        }
        if (license == null)
        {
            if (other.license != null)
            {
                return false;
            }
        }
        else if (!license.equals(other.license))
        {
            return false;
        }
        return true;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    @Override
    public String toString()
    {
        return "{key:" + key + ", license:" + license + "}";
    }
}
