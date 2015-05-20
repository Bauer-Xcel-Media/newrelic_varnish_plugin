package de.bauerexcel.newrelic.plugins.varnish;

import com.newrelic.metrics.publish.processors.EpochCounter;

/**
 * Created by jan.schumann on 20.05.15.
 */
public class MetricMeta {
    private final String unit;
    private EpochCounter counter;

    public MetricMeta(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public EpochCounter getCounter() {
        if (null == counter) {
            counter = new EpochCounter();
        }

        return counter;
    }
}
