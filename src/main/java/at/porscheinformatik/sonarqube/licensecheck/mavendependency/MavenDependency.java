package at.porscheinformatik.sonarqube.licensecheck.mavendependency;

import java.util.Objects;

public class MavenDependency implements Comparable<MavenDependency>
{
    public static final String FIELD_KEY = "key";
    public static final String FIELD_LICENSE = "license";

    private String key;
    private String license;

    public MavenDependency(String key, String license)
    {
        super();
        this.key = key;
        this.license = license;
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

        if (this.license.compareTo(o.license) == 0)
        {
            return this.key.compareTo(o.key);
        }
        else
        {
            return this.license.compareTo(o.license);
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
        MavenDependency that = (MavenDependency) o;
        return Objects.equals(key, that.key) &&
            Objects.equals(license, that.license);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, license);
    }
}
