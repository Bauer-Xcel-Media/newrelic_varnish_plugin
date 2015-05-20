package de.bauerexcel.newrelic.plugins.varnish;

import org.w3c.dom.Element;

/**
 * Created by jan.schumann on 20.05.15.
 */
public class Metric {

    private final Element element;

    public Metric(Element statsElement) {
        this.element = statsElement;
    }

    public String getName() {
        return getTagValue("name");
    }

    public String getType() {
        String type = getTagValue("type");

        return null == type ? "main" : type;
    }

    public Float getValue() {
        String value = getTagValue("value");

        return null == value ? 0.0f : Float.parseFloat(value);
    }

    public Boolean isCounter() {
        String flag = getTagValue("flag");

        return null != flag && (flag.equals("a") || flag.equals("c"));
    }

    public String getLabel() {
        return getTagValue("description");
    }

    public String getIdent() {
        return getTagValue("ident");
    }

    private String getTagValue(String tagName) {
        return this.element.getElementsByTagName(tagName).item(0) != null ? this.element.getElementsByTagName(tagName).item(0).getTextContent().toLowerCase() : null;
    }

    public Boolean hasIdent() {
        return getTagValue("ident") != null;
    }
}
