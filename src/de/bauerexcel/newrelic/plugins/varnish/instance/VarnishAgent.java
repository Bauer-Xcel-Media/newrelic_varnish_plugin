package de.bauerexcel.newrelic.plugins.varnish.instance;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.util.Logger;
import de.bauerexcel.newrelic.plugins.varnish.Metric;
import de.bauerexcel.newrelic.plugins.varnish.MetricMeta;
import de.bauerexcel.newrelic.plugins.varnish.VarnishStats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * An agent for VarnishStats.
 *
 * @author jstenhouse
 */
public class VarnishAgent extends Agent {

    private static final String GUID = "de.bauerexcel.newrelic.plugins.varnish";
    private static final String VERSION = "1.0.0";
    private static final Logger LOGGER = Logger.getLogger(VarnishAgent.class);

    private String name;
    private VarnishStats stats;
    private Map<String, MetricMeta> meta;
    private Boolean firstReport = true;
    private String agentInfo;

    /**
     * Constructor.
     */
    public VarnishAgent(String name, VarnishStats stats, Map<String, MetricMeta> meta) throws ConfigurationException {
        super(GUID, VERSION);

        this.name = name;
        this.stats = stats;
        this.meta = meta;
    }

    @Override
    public String getComponentHumanLabel() {
        return name;
    }

    @Override
    public void pollCycle() {
        LOGGER.debug("Gathering Varnish metrics. ", getAgentInfo());

        try {
            ArrayList<Metric> results = stats.fetch();   // Gather defined metrics
            reportMetrics(results);                      // Report Metrics to New Relic
        } catch (Exception e) {
            LOGGER.error("Faild to report: ", e.getMessage());
        }

        firstReport = false;
    }

    public void reportMetrics(ArrayList<Metric> results) {
        int count = 0;
        LOGGER.debug("Collected ", results.size(), " Varnish metrics. ", getAgentInfo());
        LOGGER.debug(results);

        Iterator<Metric> iter = results.iterator();
        while (iter.hasNext()) { // Iterate over current metrics
            Metric metric = iter.next();
            MetricMeta md = getMetricMeta(metric);
            if (!metric.isBitmap()) {
                if (md != null) { // Metric Meta data exists (from metric.category.json)
                    LOGGER.debug("Metric '", getMetricSpec(metric), "' = '", metric.getValue(), "'");
                    count++;

                    if (metric.isCounter()) { // Metric is a counter
                        reportMetric(getMetricSpec(metric), md.getUnit() + "/Second", md.getCounter().process(metric.getValue()));
                    } else { // Metric is a fixed Number
                        String unit = md.getUnit();
                        if (metric.isGauge()) {
                            unit += "/Second";
                        }
                        reportMetric(getMetricSpec(metric), unit, metric.getValue());
                    }
                } else { // md != null
                    if (firstReport) { // Provide some feedback of available metrics for future reporting
                        LOGGER.debug("Not reporting identified metric ", getMetricSpec(metric));
                    }
                }
            } else { // bitmap values are not supported
                if (firstReport) { // Provide some feedback of unsupported metrics for future reporting
                    LOGGER.debug("Not reporting unsupported metric ", getMetricSpec(metric));
                }
            }

        }

        LOGGER.debug("Reported to New Relic ", count, " metrics. ", getAgentInfo());
    }

    private MetricMeta getMetricMeta(Metric metric) {
        MetricMeta meta = this.meta.get(metric.getType() + "/" + metric.getName());
        if (metric.hasIdent()) {
            // if an identifier exists, multiple meta objects with the same name exsit
            if (this.meta.containsKey(metric.getType() + "/" + metric.getIdent() + "/" + metric.getName())) {
                meta = this.meta.get(metric.getType() + "/" + metric.getIdent() + "/" + metric.getName());
            }
            else {
                // we have to clone the originaly created meta object which did not contain the identifier
                meta = new MetricMeta(meta);
                this.meta.put(metric.getType() + "/" + metric.getIdent() + "/" + metric.getName(), meta);
            }
        }

        return meta;
    }

    private String getMetricSpec(Metric metric) {
        StringBuilder spec = new StringBuilder();
        spec.append("Varnish").append("/").append(metric.getType());
        if (metric.hasIdent()) {
            spec.append("/").append(metric.getIdent());
        }
        spec.append("/").append(metric.getLabel());

        return spec.toString();
    }

    private String getAgentInfo() {
        if (agentInfo == null) {
            agentInfo = new StringBuilder().append("Agent Name: ").append(name).append(". Agent Version: ").append(VERSION).toString();
        }
        return agentInfo;
    }

}
