package com.orbitflow.worker.kafka.producer;

import com.orbitflow.worker.event.StepCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepCompletedProducer {

    private static final String TOPIC = "workflow.step.completed";

    private final KafkaTemplate<String, StepCompletedEvent> kafkaTemplate;

    public void publish(StepCompletedEvent event) {
        kafkaTemplate.send(TOPIC, event.runId().toString(), event);
    }
}
