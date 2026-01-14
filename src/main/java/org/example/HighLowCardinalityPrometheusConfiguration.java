package org.example;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.micrometer.core.instrument.config.MeterFilter.forMeters;
import static io.micrometer.core.instrument.config.MeterFilter.ignoreTags;

@Configuration(proxyBeanMethods = false)
public class HighLowCardinalityPrometheusConfiguration {

    @Bean
    PrometheusMeterRegistry lowCardinalityRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        registry.config().meterFilter(forMeters(this::isCustomHighCardinalityMeter, ignoreTags("userId")));
        return registry;
    }

    @Bean
    PrometheusMeterRegistry highCardinalityRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    // @Qualifier is only needed here because the name of the parameter is registry and not lowCardinalityRegistry (the name of the bean).
    // If the name of the parameter would be lowCardinalityRegistry, @Qualifier would not be needed, see the next method.
    @Bean
    PrometheusScrapeEndpoint prometheusEndpoint(@Qualifier("lowCardinalityRegistry") PrometheusMeterRegistry registry, PrometheusConfig prometheusConfig) {
        return new PrometheusScrapeEndpoint(registry.getPrometheusRegistry(), prometheusConfig.prometheusProperties());
    }

    @Bean
    PrometheusHighCardinalityScrapeEndpoint prometheusHighCardinalityScrapeEndpoint(PrometheusMeterRegistry highCardinalityRegistry, PrometheusConfig prometheusConfig) {
        return new PrometheusHighCardinalityScrapeEndpoint(highCardinalityRegistry.getPrometheusRegistry(), prometheusConfig.prometheusProperties());
    }

    @Bean
    HighCardinalityMeterObservationHandler highCardinalityMeterObservationHandler(MeterRegistry registry) {
        return new HighCardinalityMeterObservationHandler(registry, this::isCustomHighCardinalityObservation);
    }

    private boolean isCustomHighCardinalityObservation(Observation.Context context) {
        return "mcp.tool.call".equals(context.getName())
            || "metered.mcp.tool.call".equals(context.getName());
    }

    private boolean isCustomHighCardinalityMeter(Meter.Id id) {
        return "mcp.tool.call".equals(id.getName()) || id.getName().startsWith("mcp.tool.call.")
            || "metered.mcp.tool.call".equals(id.getName()) || id.getName().startsWith("metered.mcp.tool.call.");
    }
}
