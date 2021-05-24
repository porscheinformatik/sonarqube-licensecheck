package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

class SettingsXmlParser extends SettingsXmlHandler
{
    private static final Logger LOGGER = Loggers.get(SettingsXmlParser.class);

    private SettingsXmlParser()
    {
    }

    public static Setting parseXmlFile(File filePath)
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SettingsXmlHandler settingsXmlHandler = new SettingsXmlHandler();
        SAXParser saxParser;

        if (filePath.exists())
        {
            try
            {
                saxParser = saxParserFactory.newSAXParser();
                saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                saxParser.parse(filePath, settingsXmlHandler);
            }
            catch (Exception e)
            {
                LOGGER.warn("Could not parse file " + filePath, e);
            }
        }
        else
        {
            LOGGER.info("Could not find file " + filePath);
        }

        return settingsXmlHandler.getSetting();
    }
}
