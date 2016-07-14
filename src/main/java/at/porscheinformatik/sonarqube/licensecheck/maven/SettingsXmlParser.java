package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SettingsXmlParser extends SettingsXmlHandler
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsXmlParser.class);

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
