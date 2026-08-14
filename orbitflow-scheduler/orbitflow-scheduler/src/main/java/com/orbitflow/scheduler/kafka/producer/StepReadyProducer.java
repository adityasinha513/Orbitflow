package com.orbitflow.scheduler.kafka.producer;

import com.orbitflow.scheduler.event.StepReadyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepReadyProducer {

    private static final String TOPIC = "workflow.step.ready";

    private final KafkaTemplate<String, StepReadyEvent> kafkaTemplate;

    public void publish(StepReadyEvent event) {
        // Keyed by job_run_id so all steps of one run land on the same partition and stay ordered.
        kafkaTemplate.send(TOPIC, event.runId().toString(), event);
    }
}
