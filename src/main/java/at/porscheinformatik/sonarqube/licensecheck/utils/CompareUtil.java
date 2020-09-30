package at.porscheinformatik.sonarqube.licensecheck.utils;

public class CompareUtil
{
    private CompareUtil()
    {
    }

    public static boolean equals(Object a, Object b)
    {
        if (a == b)
        {
            return true;
        }

        return a != null && b != null && a.getClass() == b.getClass();
    }
}
