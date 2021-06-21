package at.porscheinformatik.sonarqube.licensecheck.dependencymapping;

import java.util.Objects;

public class DependencyMapping implements Comparable<DependencyMapping>
{
    public static final String FIELD_KEY = "key";
    public static final String FIELD_LICENSE = "license";
    public static final String FIELD_OVERWRITE = "overwrite";

    private String key;
    private String license;
    private Boolean overwrite;

    public DependencyMapping(String key, String license, Boolean overwrite)
    {
        super();
        this.key = key;
        this.license = license;
        this.overwrite = overwrite;
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

    public Boolean getOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite(Boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    @Override
    public String toString()
    {
        return "{key:" + key + ", license:" + license + "}";
    }

    @Override
    public int compareTo(DependencyMapping o)
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
        DependencyMapping that = (DependencyMapping) o;
        return Objects.equals(key, that.key) &&
            Objects.equals(license, that.license);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, license);
    }
}
