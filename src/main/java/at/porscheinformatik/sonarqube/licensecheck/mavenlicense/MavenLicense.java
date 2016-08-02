package at.porscheinformatik.sonarqube.licensecheck.mavenlicense;

import java.util.regex.Pattern;

public class MavenLicense implements Comparable<MavenLicense>
{
    private Pattern licenseNameRegEx;
    private String licenseKey;

    public MavenLicense(String licenseNameRegEx, String licenseKey)
    {
        super();
        this.licenseNameRegEx = Pattern.compile(licenseNameRegEx);
        this.licenseKey = licenseKey;
    }

    public Pattern getLicenseNameRegEx()
    {
        return licenseNameRegEx;
    }

    public void setLicenseNameRegEx(String licenseNameRegEx)
    {
        this.licenseNameRegEx = Pattern.compile(licenseNameRegEx);
    }

    public void setLicenseNameRegEx(Pattern licenseNameRegEx)
    {
        this.licenseNameRegEx = licenseNameRegEx;
    }

    public String getLicenseKey()
    {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey)
    {
        this.licenseKey = licenseKey;
    }

    @Override
    public int compareTo(MavenLicense o)
    {
        if (o == null)
        {
            return 1;
        }
        else if (this.licenseKey.compareTo(o.licenseKey) == 0)
        {
            return this.licenseNameRegEx.toString().compareTo(o.licenseNameRegEx.toString());
        }
        else
        {
            return this.licenseKey.compareTo(o.licenseKey);
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
        if (mavenLicense.licenseKey.equals(this.licenseKey)
            && mavenLicense.licenseNameRegEx.toString().equals(this.licenseNameRegEx.toString()))
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
        result = (prime * result) + ((licenseKey == null) ? 0 : licenseKey.hashCode());
        result = (prime * result) + ((licenseNameRegEx == null) ? 0 : licenseNameRegEx.hashCode());
        return result;
    }
}
