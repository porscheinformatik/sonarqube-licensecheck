package at.porscheinformatik.sonarqube.licensecheck.maven;

import java.util.ArrayList;
import java.util.List;

class ElementStack
{

    private List<String> elementStack;

    public ElementStack()
    {
        elementStack = new ArrayList<String>();
    }

    public void push(String label)
    {
        elementStack.add(label);
    }

    public String pop()
    {
        String tmp = elementStack.get(elementStack.size() - 1);
        elementStack.remove(elementStack.size() - 1);
        return tmp;
    }

    public String getTopElement()
    {
        int tmp = elementStack.size() - 2;

        //in case the very first level is requested
        if (tmp < 0)
        {
            return elementStack.get(elementStack.size() - 1);
        }
        else
        {
            return elementStack.get(elementStack.size() - 2);
        }

    }

    public String getCurrentElement()
    {
        return elementStack.get(elementStack.size() - 1);
    }
}
