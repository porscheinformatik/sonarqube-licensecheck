package at.porscheinformatik.sonarqube.licensecheck.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class IOUtils
{
    private IOUtils()
    {
    }

    public static String readToString(InputStream input) throws IOException 
    {
        Reader in = new InputStreamReader(input, StandardCharsets.UTF_8);
        StringWriter out = new StringWriter();
        int n = 0;
        char[] buffer = new char[4096];
        while ((n = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, n);
        }
        return out.toString();
    }
}
