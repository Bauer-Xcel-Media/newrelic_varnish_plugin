package de.bauerexcel.newrelic.plugins.varnish;

import com.newrelic.metrics.publish.processors.EpochCounter;

/**
 * Contains metadata for a metric
 * <p/>
 * - the unit name as configured through plugin.json
 * - previous values are kept through an EpochCounter if needed.
 * NOTE that the fact if a metric is a counter or not
 * is not a part of the metadata, as it is kept in the metrics themselves
 *
 * @author Jan Schumann <jan.schumann@bauerexcel.de>
 * @see Metric
 */

public class MetricMeta {
    private final String unit;
    private EpochCounter counter;

    public MetricMeta(String unit) {
        this.unit = unit;
    }

    // used for cloning
    public MetricMeta(MetricMeta meta) {
        this.unit = meta.getUnit();
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
