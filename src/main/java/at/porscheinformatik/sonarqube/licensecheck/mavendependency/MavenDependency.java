package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

public class MavenDependency implements Comparable<MavenDependency>
{
    private String key;
    private String license;

    public MavenDependency(String key, String license)
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

    @Override
    public int compareTo(MavenDependency o)
    {
        if (o == null)
        {
            return 1;
        }
        else if (this.license.compareTo(o.license) == 0)
        {
            return this.key.compareTo(o.key);
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

        MavenDependency mavenDependency = (MavenDependency) object;
        return mavenDependency.license.equals(this.license) && mavenDependency.key.equals(this.key);
    }
}
