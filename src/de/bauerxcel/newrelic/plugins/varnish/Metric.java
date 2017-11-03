package de.bauerxcel.newrelic.plugins.varnish;

import org.w3c.dom.Element;

/**
 * Represents a metric provided by varnishstats
 *
 * @author Jan Schumann <jan.schumann@bauerxcel.de>
 */
public class Metric {
    private final Element element;
    private final String type;
    private final String name;
    private final String ident;
    private final Float value;
    private final String flag;
    private final String description;

    public Metric(Element statsElement) {
        this.element = statsElement;
        String type = getTagValue("type");

        // varnish version 5.2
        if (null == type) {
            // MGT.uptime = <type>.<name>
            // MEMPOOL.busyobj.live = <type>.<ident>.<name>
            // VBE.boot.default.bereq_hdrbytes = <type>.<ident>.<name> (ident = boot.default)
            String name = getTagValue("name");
            int firstDot = name.indexOf(".");
            int lastDot = name.lastIndexOf(".");

            this.type = name.substring(0, firstDot);
            if (firstDot == lastDot) {
                this.ident = null;
                this.name = name.substring(firstDot + 1);
            } else {
                this.ident = name.substring(firstDot + 1, lastDot);
                this.name = name.substring(lastDot + 1);
            }
        } else {
            this.type = type;
            this.name = getTagValue("name");
            this.ident = getTagValue("ident");
        }

        this.flag = getTagValue("flag");
        this.description = getTagValue("description");

        String value = getTagValue("value");
        if (null == value) {
            this.value = 0.0f;
        } else {
            this.value = Float.parseFloat(value);
        }

    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return null == this.type ? "main" : this.type;
    }

    public Float getValue() {
        return this.value;
    }

    /**
     * Determine if this flag is a counter.
     * <br/>
     * Possible values for the plag property are
     * <br/>
     * - 'a' - Accumulator (deprecated, use 'c')
     * - 'b' - Bitmap
     * - 'c' - Counter, never decreases.
     * - 'g' - Gauge, goes up and down
     * - 'i' - Integer (deprecated, use 'g')
     *
     * @return Boolean
     * @todo the a flag check can be removed as soon as the next varnish release arrives
     */
    public Boolean isCounter() {
        return null != this.flag && (this.flag.equals("a") || this.flag.equals("c"));
    }

    /**
     * Determine if this flag is a gauge, which means that the provided value
     * should be treated as a rate. (unit/second)
     *
     * @return Boolean
     */
    public Boolean isGauge() {
        return null != this.flag && this.flag.equals("g");
    }

    /**
     * Determine if this flag is a bitmap.
     * e.g. PER BACKEND COUNTERS/happy
     *
     * @return Boolean
     */
    public Boolean isBitmap() {
        return null != this.flag && this.flag.equals("b");
    }

    public String getDescription() {
        return this.description;
    }

    public String getIdent() {
        return this.ident;
    }

    public Boolean hasIdent() {
        return null != this.ident;
    }

    private String getTagValue(String tagName) {
        return this.element.getElementsByTagName(tagName).item(0) != null ? this.element.getElementsByTagName(tagName).item(0).getTextContent().toLowerCase() : null;
    }
}
