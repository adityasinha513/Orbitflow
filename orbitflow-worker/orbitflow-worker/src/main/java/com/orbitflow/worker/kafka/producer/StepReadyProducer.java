package com.orbitflow.worker.kafka.producer;

import com.orbitflow.worker.event.StepReadyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Used only for retry redispatch - the initial dispatch of a step is always the scheduler's job. */
@Component
@RequiredArgsConstructor
public class StepReadyProducer {

    private static final String TOPIC = "workflow.step.ready";

    private final KafkaTemplate<String, StepReadyEvent> kafkaTemplate;

    public void publish(StepReadyEvent event) {
        kafkaTemplate.send(TOPIC, event.runId().toString(), event);
    }
}
