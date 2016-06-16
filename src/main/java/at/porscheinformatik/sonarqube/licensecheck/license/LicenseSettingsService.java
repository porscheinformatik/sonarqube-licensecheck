package at.porscheinformatik.sonarqube.licensecheck.license;

import at.porscheinformatik.sonarqube.licensecheck.dependency.DependencySettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;
import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_REGEX;

@ServerSide
public class LicenseSettingsService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseSettingsService.class);

    /** This is not official API */
    private final PersistentSettings persistentSettings;

    private final Settings settings;
    private final LicenseService licenseService;
    private final DependencySettingsService dependencySettingsService;

    public LicenseSettingsService(PersistentSettings persistentSettings, LicenseService licenseService,
        DependencySettingsService dependencySettingsService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.settings = persistentSettings.getSettings();
        this.licenseService = licenseService;
        this.dependencySettingsService = dependencySettingsService;

        initSpdxLicences();
    }

    public String getLicensesForTable()
    {
        String licenses = settings.getString(LICENSE_KEY);

        if (licenses == null || licenses.isEmpty())
        {
            initSpdxLicences();
        }
        List<License> licensesList = licenseService.getLicenses();
        saveSettings(licensesList);

        StringBuilder licenseString = new StringBuilder();
        for (License license : licensesList)
        {
            licenseString.append(license.getIdentifier()).append("~");
            licenseString.append(license.getName()).append("~");
            licenseString.append(license.getStatus()).append(";");
        }
        return licenseString.toString();
    }

    public String getLicensesID()
    {
        List<License> licenses = licenseService.getLicenses();

        StringBuilder licenseString = new StringBuilder();
        for (License license : licenses)
        {
            licenseString.append(license.getIdentifier()).append(";");
        }
        return licenseString.toString();
    }

    public void addLicense(String id, String name, String status)
    {
        String licenseString = settings.getString(LICENSE_KEY);
        if (!licenseString.contains(id) && !licenseString.contains(name))
        {
            JsonObject jsonObject = Json.createObjectBuilder()
                .add(id, Json.createObjectBuilder()
                    .add("name", name)
                    .add("status", status))
                .build();

            licenseString = licenseString + jsonObject.toString();

            if (licenseString.contains("}{"))
            {
                String addedLicenses = licenseString.replace("}{", ", ");
                settings.setProperty(LICENSE_KEY, addedLicenses);
                persistentSettings.saveProperty(LICENSE_KEY, addedLicenses);
            }
            else
            {
                settings.setProperty(LICENSE_KEY, licenseString);
                persistentSettings.saveProperty(LICENSE_KEY, licenseString);
            }
        }
        else
        {
            LOGGER.info("License already exists!");
        }
    }

    public void addLicenseRegex(String license_name, String license_key)
    {
        String licenseString = settings.getString(LICENSE_REGEX);

        if (licenseString == null)
        {
            licenseString = "";
        }

        if (!licenseString.matches(".*" + license_name + ".*"))
        {
            licenseString = licenseString + license_name + "~" + license_key + ";";
            settings.setProperty(LICENSE_REGEX, licenseString);
            persistentSettings.saveProperty(LICENSE_REGEX, licenseString);
        }
        else
        {
            LOGGER.info("License REGEX already exists!");
        }
    }

    public String getLicensesRegexForTable()
    {
        return licenseService.getLicensesRegex();
    }

    public void deleteLicenseRegex(String id)
    {
        String licenseString = settings.getString(LICENSE_REGEX);
        String newLicenseString = "";
        String[] parts = licenseString.split(";");
        for (String s : parts)
        {
            if (!s.equals(id))
            {
                newLicenseString = newLicenseString + s + ";";
            }
        }
        settings.setProperty(LICENSE_REGEX, newLicenseString);
        persistentSettings.saveProperty(LICENSE_REGEX, newLicenseString);
    }

    public void deleteLicense(String id)
    {
        final String[] identifierName = id.split("~");

        List<License> newLicenseList = new ArrayList<>();
        JsonParser parser = Json.createParser(new StringReader(settings.getString(LICENSE_KEY)));
        JsonReader jsonReader = Json.createReader(new StringReader(settings.getString(LICENSE_KEY)));
        final JsonObject jsonObject = jsonReader.readObject();

        while (parser.hasNext())
        {
            JsonParser.Event event = parser.next();
            switch (event)
            {
                case KEY_NAME:
                    if (!"name".equals(parser.getString()) && !"url".equals(parser.getString())
                        && !"osiApproved".equals(parser.getString()) && !"status".equals(parser.getString()))
                    {
                        newLicenseList =
                            deleteLicensesCheck(newLicenseList, parser.getString(), identifierName[0], jsonObject);
                    }
                    break;
                default:
                    break;
            }
        }
        saveSettings(newLicenseList);
    }

    private List<License> deleteLicensesCheck(List<License> newLicenseList, String identifier,
        String identifierToDelete, JsonObject jsonObject)
    {
        JsonObject identifierObj = jsonObject.getJsonObject(identifier);
        if (!identifier.equals(identifierToDelete))
        {

            if (identifierObj.containsKey("status"))
            {
                newLicenseList.add(new License(identifierObj.getString("name"), identifier,
                    identifierObj.getString("status")));
            }
            else
            {
                newLicenseList
                    .add(new License(identifierObj.getString("name"), identifier, "false"));
            }
        }
        return newLicenseList;
    }

    public void updateLicense(final String id, final String name, final String status)
    {
        String licenseString = settings.getString(LICENSE_KEY);

        final List<License> newLicenseList = new ArrayList<>();

        if (licenseString.contains(id) || licenseString.contains(name))
        {
            updateLicensesParser(newLicenseList, licenseString, id, name, status);
            saveSettings(newLicenseList);
        }
    }

    public void updateMavenLicense(String key, String name, String identifier)
    {
        dependencySettingsService.addAllowedDependency(key, identifier);
    }

    private void updateLicensesParser(List<License> newLicenseList, String licenseString, String id, String name,
        String status)
    {
        JsonParser parser = Json.createParser(new StringReader(licenseString));
        JsonReader jsonReader = Json.createReader(new StringReader(licenseString));
        final JsonObject jsonObject = jsonReader.readObject();

        while (parser.hasNext())
        {
            JsonParser.Event event = parser.next();
            switch (event)
            {
                case KEY_NAME:
                    if (!"name".equals(parser.getString()) && !"url".equals(parser.getString())
                        && !"osiApproved".equals(parser.getString()) && !"status".equals(parser.getString()))
                    {
                        updateLicensesCheck(newLicenseList, jsonObject, parser.getString(), id, name, status);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void updateLicensesCheck(List<License> newLicenseList, JsonObject jsonObject, String identifier,
        String id, String name, String status)
    {
        JsonObject identifierObj = jsonObject.getJsonObject(identifier);
        if (identifier.equals(id))
        {
            newLicenseList.add(new License(name, identifier, status));
        }
        else
        {
            newLicenseList.add(new License(identifierObj.getString("name"), identifier,
                identifierObj.getString("status")));
        }
    }

    private void saveSettings(List<License> licenseList)
    {
        String newJsonLicense = "";

        for (License license : licenseList)
        {
            if (license.getName().contains("\""))
            {
                JsonObject jsonObject = Json.createObjectBuilder()
                    .add(license.getIdentifier(), Json.createObjectBuilder()
                        .add("name", license.getName().replace("\"", ""))
                        .add("status", license.getStatus()))
                    .build();

                newJsonLicense = newJsonLicense + jsonObject.toString();
            }
            else
            {
                JsonObject jsonObject = Json.createObjectBuilder()
                    .add(license.getIdentifier(), Json.createObjectBuilder()
                        .add("name", license.getName())
                        .add("status", license.getStatus()))
                    .build();

                newJsonLicense = newJsonLicense + jsonObject.toString();
            }
            if (newJsonLicense.contains("}{"))
            {
                newJsonLicense = newJsonLicense.replace("}{", ", ");
            }
        }
        settings.setProperty(LICENSE_KEY, newJsonLicense);
        persistentSettings.saveProperty(LICENSE_KEY, newJsonLicense);
    }

    private void initSpdxLicences()
    {
        String licenseJson = settings.getString(LICENSE_KEY);

        if ((licenseJson != null) && !licenseJson.isEmpty())
        {
            return;
        }

        try
        {
            InputStream inputStream = LicenseService.class.getResourceAsStream("spdx_license_list.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                out.append(line);
            }
            String newJsonLicense = out.toString();
            settings.setProperty(LICENSE_KEY, newJsonLicense);
            persistentSettings.saveProperty(LICENSE_KEY, newJsonLicense);
            reader.close();
            inputStream.close();
        }
        catch (Exception e)
        {
            LOGGER.error("Could not load spdx_license_list.json", e);
        }
    }
}
