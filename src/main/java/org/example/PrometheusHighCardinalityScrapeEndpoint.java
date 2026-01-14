package org.example;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;

import java.util.Properties;

@WebEndpoint(id = "prometheus-all")
public class PrometheusHighCardinalityScrapeEndpoint extends PrometheusScrapeEndpoint {
    public PrometheusHighCardinalityScrapeEndpoint(PrometheusRegistry prometheusRegistry, @Nullable Properties exporterProperties) {
        super(prometheusRegistry, exporterProperties);
    }
}
