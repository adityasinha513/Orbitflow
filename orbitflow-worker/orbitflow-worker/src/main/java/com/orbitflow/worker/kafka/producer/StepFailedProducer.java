package com.orbitflow.worker.kafka.producer;

import com.orbitflow.worker.event.StepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepFailedProducer {

    private static final String TOPIC = "workflow.step.failed";

    private final KafkaTemplate<String, StepFailedEvent> kafkaTemplate;

    public void publish(StepFailedEvent event) {
        kafkaTemplate.send(TOPIC, event.runId().toString(), event);
    }
}
