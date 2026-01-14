package org.example;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToolCallController {

    private final ObservationRegistry observationRegistry;
    private final MeterRegistry meterRegistry;

    public ToolCallController(ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
        this.observationRegistry = observationRegistry;
        this.meterRegistry = meterRegistry; // this will be the composite
    }

    @GetMapping("/toolCall")
    public String toolCall() {
        // This is not high cardinality here but in a real life application it will be
        String userId = Math.random() > 0.5 ? "adib" : "marcela";
        // When you stop an Observation a Timer (that contains a counter) is created
        // Because this creates a Timer, it also tracks
        //  - the total time
        //  - the max time (time-windowed, see later)
        // For example let's say you call the remote server twice, the first call takes 10ms the second one takes 20ms:
        //  - count: 2 (two calls)
        //  - total: 30ms (10+20ms)
        //  - max: 20ms (the highest)
        // NOTE: max is a time-window max, see: https://docs.micrometer.io/micrometer/reference/concepts/timers.html
        int duration = Observation.createNotStarted("mcp.tool.call", observationRegistry)
                .lowCardinalityKeyValue("mcp.server.name", "GitHub")
                .lowCardinalityKeyValue("tool.name", "readIssue")
                .highCardinalityKeyValue("userId", userId)
                .observe(this::invokeRemoteServer);
        return "Tool call for user %s took %d ms".formatted(userId, duration);
    }

    @GetMapping("/meteredToolCall")
    public String meteredToolCall() {
        String userId = Math.random() > 0.5 ? "adib" : "marcela";
        Counter.builder("metered.mcp.tool.call")
                .tag("mcp.server.name", "GitHub")
                .tag("tool.name", "readIssue")
                .tag("userId", userId) // this is high cardinality
                .register(meterRegistry)
                .increment();
        return "Tool call for user %s was recorded".formatted(userId);
    }

    private int invokeRemoteServer() {
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
