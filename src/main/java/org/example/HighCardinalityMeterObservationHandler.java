package org.example;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.Observation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HighCardinalityMeterObservationHandler extends DefaultMeterObservationHandler {

    private final MeterRegistry meterRegistry;

    private final boolean shouldCreateLongTaskTimer;

    private final Predicate<Observation.Context> shouldIncludeHighCardinality;

    public HighCardinalityMeterObservationHandler(MeterRegistry meterRegistry, Predicate<Observation.Context> shouldIncludeHighCardinality) {
        super(meterRegistry);
        this.meterRegistry = meterRegistry;
        this.shouldCreateLongTaskTimer = true;
        this.shouldIncludeHighCardinality = shouldIncludeHighCardinality;
    }

    @Override
    public void onStart(Observation.Context context) {
        if (shouldCreateLongTaskTimer) {
            LongTaskTimer.Sample longTaskSample = meterRegistry.more()
                    .longTaskTimer(context.getName() + ".active", createTags(context))
                    .start();
            context.put(LongTaskTimer.Sample.class, longTaskSample);
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        context.put(Timer.Sample.class, sample);
    }

    @Override
    public void onStop(Observation.Context context) {
        List<Tag> tags = createTags(context);
        tags.add(Tag.of("error", getErrorValue(context)));
        Timer.Sample sample = context.getRequired(Timer.Sample.class);
        sample.stop(this.meterRegistry.timer(context.getName(), tags));

        if (shouldCreateLongTaskTimer) {
            LongTaskTimer.Sample longTaskSample = context.getRequired(LongTaskTimer.Sample.class);
            longTaskSample.stop();
        }
    }

    @Override
    public void onEvent(Observation.Event event, Observation.Context context) {
        Counter.builder(context.getName() + "." + event.getName())
                .tags(createTags(context))
                .register(meterRegistry)
                .increment();
    }

    private String getErrorValue(Observation.Context context) {
        Throwable error = context.getError();
        return error != null ? error.getClass().getSimpleName() : KeyValue.NONE_VALUE;
    }

    private List<Tag> createTags(Observation.Context context) {
        KeyValues keyValues = shouldIncludeHighCardinality.test(context) ? context.getAllKeyValues() : context.getLowCardinalityKeyValues();
        List<Tag> tags = new ArrayList<>();
        for (KeyValue keyValue : keyValues) {
            tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
        }
        return tags;
    }


}
