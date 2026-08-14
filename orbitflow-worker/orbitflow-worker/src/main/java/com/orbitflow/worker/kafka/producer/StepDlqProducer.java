package com.orbitflow.worker.kafka.producer;

import com.orbitflow.worker.event.StepDlqEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepDlqProducer {

    private static final String TOPIC = "workflow.step.dlq";

    private final KafkaTemplate<String, StepDlqEvent> kafkaTemplate;

    public void publish(StepDlqEvent event) {
        kafkaTemplate.send(TOPIC, event.runId().toString(), event);
    }
}
