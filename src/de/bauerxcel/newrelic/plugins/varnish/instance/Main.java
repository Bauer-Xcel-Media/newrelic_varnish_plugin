package de.bauerxcel.newrelic.plugins.varnish.instance;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

/**
 * Main class for Varnish Agent
 *
 * @author Jan Schumann <jan.schumann@bauerxcel.de>
 */
public class Main {

    public static void main(String[] args) {
        try {
            Runner runner = new Runner();
            runner.add(new VarnishAgentFactory());
            runner.setupAndRun(); // Never returns
        } catch (ConfigurationException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(-1);
        }
    }
}
