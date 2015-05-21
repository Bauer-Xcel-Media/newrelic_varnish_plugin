package de.bauerexcel.newrelic.plugins.varnish.instance;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import de.bauerexcel.newrelic.plugins.varnish.MetricMeta;
import de.bauerexcel.newrelic.plugins.varnish.VarnishStats;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * AgentFactory
 *
 * @author jschumann
 */
public class VarnishAgentFactory extends AgentFactory {

    @Override
    public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
        if (!properties.containsKey("name")) {
            throw new ConfigurationException("The 'name' attribute is required. Have you configured the 'config/plugin.json' file?");
        }

        // agent name
        String name = (String) properties.get("name");

        // stats
        VarnishStats stats = new VarnishStats();
        if (properties.containsKey("instance")) {
            stats.setInstance((String) properties.get("instance"));
        }
        if (properties.containsKey("user") || properties.containsKey("host")) {
            stats.setSshCommand(buildSshCommand(properties));
        }
        if (!stats.isValid()) {
            throw new ConfigurationException("Executing '" + stats.getCommand() + "' failed: " + stats.getError() + ". Please check the 'config/plugin.json' file.");
        }

        // metrics meta
        JSONObject metrics = Config.getValue("metric_units");
        if (null == metrics) {
            throw new ConfigurationException("The 'metric_units' attribute is required. Have you configured the 'config/plugin.json' file?");
        }
        Map<String, MetricMeta> meta = buildMetricMeta(metrics);

        return new VarnishAgent(name, stats, meta);
    }

    private String buildSshCommand(Map<String, Object> properties) throws ConfigurationException {
        String sshCommand = "";

        String user = (String) properties.get("user");
        String host = (String) properties.get("host");
        if (!properties.containsKey("user")) {
            throw new ConfigurationException("This agent is configured to connect via ssh using host '" + host + "', but no user is given. Please check your 'config/plugin.json' file.");
        }
        if (!properties.containsKey("host")) {
            throw new ConfigurationException("This agent is configured to connect via ssh using user '" + user + "', but no host is given. Please check your 'config/plugin.json' file.");
        }

        sshCommand += "ssh -t " + user + "@" + host;

        if (properties.containsKey("identity")) {
            sshCommand += " -i " + properties.get("identity");
        }
        if (properties.containsKey("port")) {
            sshCommand += " -p " + properties.get("port");
        }
        if (properties.containsKey("options")) {
            sshCommand += " " + properties.get("options");
        }

        return sshCommand;
    }

    private Map<String, MetricMeta> buildMetricMeta(JSONObject metrics) {
        Map<String, MetricMeta> meta = new HashMap();

        Iterator<?> typeIterator = metrics.keySet().iterator();
        while (typeIterator.hasNext()) {                            // Iterate over types
            String type = (String) typeIterator.next();
            JSONObject units = (JSONObject) metrics.get(type);
            Iterator<?> unitIterator = units.keySet().iterator();
            while (unitIterator.hasNext()) {                        // Iterate over units in type
                String name = (String) unitIterator.next();
                String unit = (String) units.get(name);
                MetricMeta metricMeta = new MetricMeta(unit);
                meta.put(type + "/" + name, metricMeta);
            }
        }

        return meta;
    }
}
