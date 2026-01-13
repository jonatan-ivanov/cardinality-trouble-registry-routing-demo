package org.example;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CardinalityTroubleController {

    private final ObservationRegistry observationRegistry;
    private final PrometheusMeterRegistry lowCardinalityRegistry;
    private final PrometheusMeterRegistry highCardinalityRegistry;

    public CardinalityTroubleController(ObservationRegistry observationRegistry, PrometheusMeterRegistry lowCardinalityRegistry, PrometheusMeterRegistry highCardinalityRegistry) {
        this.observationRegistry = observationRegistry;
        this.lowCardinalityRegistry = lowCardinalityRegistry;
        this.highCardinalityRegistry = highCardinalityRegistry;
    }

    @GetMapping(value = "/low", produces = "text/plain")
    public String low() {
        return lowCardinalityRegistry.scrape();
    }

    @GetMapping(value = "/high", produces = "text/plain")
    public String high() {
        return highCardinalityRegistry.scrape();
    }

    @GetMapping("/random")
    public String random() {
        String uuid = UUID.randomUUID().toString();
        int duration = Observation.createNotStarted("random", observationRegistry)
                .highCardinalityKeyValue("uuid", uuid)
                .observe(this::randomSleep);
        return "%s slept for %d ms".formatted(uuid, duration);
    }

    private int randomSleep() {
        int duration = (int)(Math.random() * 100 + 100); // 100-200ms
        try {
            Thread.sleep(duration);
        }
        catch (InterruptedException e) {
            // intentionally ignored
        }
        return duration;
    }
}
