package at.porscheinformatik.sonarqube.licensecheck.license;

import static at.porscheinformatik.sonarqube.licensecheck.LicenseCheckPropertyKeys.LICENSE_KEY;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;
import org.sonar.server.platform.PersistentSettings;

@ServerSide
public class LicenseSettingsService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseSettingsService.class);

    /** This is not official API */
    private final PersistentSettings persistentSettings;

    private final Settings settings;
    private final LicenseService licenseService;

    public LicenseSettingsService(PersistentSettings persistentSettings, LicenseService licenseService)
    {
        super();
        this.persistentSettings = persistentSettings;
        this.settings = persistentSettings.getSettings();
        this.licenseService = licenseService;

        initSpdxLicences();
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

    public boolean addLicense(String name, String identifier, String status)
    {
        License newLicense = new License(name, identifier, status);
        return addLicense(newLicense);
    }

    public boolean addLicense(License newLicense)
    {
        List<License> licenses = licenseService.getLicenses();

        if (!checkIfListContains(newLicense))
        {
            licenses.add(newLicense);
            saveSettings(licenses);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean checkIfListContains(License license)
    {
        List<License> licenses = licenseService.getLicenses();
        return licenses.contains(license);
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
                    if (!"name".equals(parser.getString())
                        && !"url".equals(parser.getString())
                        && !"osiApproved".equals(parser.getString())
                        && !"status".equals(parser.getString()))
                    {
                        newLicenseList =
                            deleteLicensesCheck(newLicenseList, parser.getString(), identifierName[0], jsonObject);
                    }
                    break;
                default:
                    break;
            }
        }

        parser.close();
        jsonReader.close();

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
                newLicenseList
                    .add(new License(identifierObj.getString("name"), identifier, identifierObj.getString("status")));
            }
            else
            {
                newLicenseList.add(new License(identifierObj.getString("name"), identifier, "false"));
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
                    if (!"name".equals(parser.getString())
                        && !"url".equals(parser.getString())
                        && !"osiApproved".equals(parser.getString())
                        && !"status".equals(parser.getString()))
                    {
                        updateLicensesCheck(newLicenseList, jsonObject, parser.getString(), id, name, status);
                    }
                    break;
                default:
                    break;
            }
        }

        parser.close();
        jsonReader.close();
    }

    private void updateLicensesCheck(List<License> newLicenseList, JsonObject jsonObject, String identifier, String id,
        String name, String status)
    {
        JsonObject identifierObj = jsonObject.getJsonObject(identifier);
        if (identifier.equals(id))
        {
            newLicenseList.add(new License(name, identifier, status));
        }
        else
        {
            newLicenseList
                .add(new License(identifierObj.getString("name"), identifier, identifierObj.getString("status")));
        }
    }

    private void saveSettings(List<License> licenseList)
    {

        String newJsonLicense = "";

        for (License license : licenseList)
        {
            if (license.getName().contains("\""))
            {
                JsonObject jsonObject = Json
                    .createObjectBuilder()
                    .add(license.getIdentifier(),
                        Json.createObjectBuilder().add("name", license.getName().replace("\"", "")).add("status",
                            license.getStatus()))
                    .build();

                newJsonLicense = newJsonLicense + jsonObject.toString();
            }
            else
            {
                JsonObject jsonObject = Json
                    .createObjectBuilder()
                    .add(license.getIdentifier(),
                        Json.createObjectBuilder().add("name", license.getName()).add("status", license.getStatus()))
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

    public void sortLicenses()
    {
        List<License> licenseList = licenseService.getLicenses();
        Collections.sort(licenseList);
        saveSettings(licenseList);
    }
}
