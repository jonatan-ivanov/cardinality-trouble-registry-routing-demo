package org.example;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.observation.Observation;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CardinalityTroubleApplication {

	static void main(String[] args) {
		SpringApplication.run(CardinalityTroubleApplication.class, args);
	}

	@Bean
	PrometheusMeterRegistry lowCardinalityRegistry() {
		PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
		registry.config().meterFilter(MeterFilter.deny(this::isCustomHighCardinalityMeter));
		return registry;
	}

	@Bean
	PrometheusMeterRegistry highCardinalityRegistry() {
		PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
		registry.config().meterFilter(MeterFilter.denyUnless(this::isCustomHighCardinalityMeter));
		return registry;
	}

	@Bean
	HighCardinalityMeterObservationHandler highCardinalityMeterObservationHandler(MeterRegistry registry) {
		return new HighCardinalityMeterObservationHandler(registry, this::isCustomHighCardinalityObservation);
	}

	private boolean isCustomHighCardinalityObservation(Observation.Context context) {
		return "random".equals(context.getName());
	}

	private boolean isCustomHighCardinalityMeter(Meter.Id id) {
		return "random".equals(id.getName());
	}
}
