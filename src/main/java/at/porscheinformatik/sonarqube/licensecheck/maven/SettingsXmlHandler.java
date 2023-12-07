package at.porscheinformatik.sonarqube.licensecheck.maven;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class SettingsXmlHandler extends DefaultHandler {

    private boolean enableReadElementData = false;
    private String tagName = "";
    private Setting setting;

    @Override
    public void startDocument() {
        setting = new Setting();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equals("localRepository")) {
            tagName = "localRepository";
        } else {
            tagName = "";
        }

        enableReadElementData = true;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        enableReadElementData = false;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (enableReadElementData && tagName.equals("localRepository")) {
            setting.setLocalRepositoryPath(new String(ch, start, length));
        }
    }

    public Setting getSetting() {
        return setting;
    }
}
