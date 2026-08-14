package com.orbitflow.worker.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitflow.worker.entity.JobStep;
import com.orbitflow.worker.entity.StepStatus;
import com.orbitflow.worker.event.StepCompletedEvent;
import com.orbitflow.worker.event.StepFailedEvent;
import com.orbitflow.worker.event.StepReadyEvent;
import com.orbitflow.worker.kafka.producer.StepCompletedProducer;
import com.orbitflow.worker.kafka.producer.StepFailedProducer;
import com.orbitflow.worker.service.StepProcessingResult;
import com.orbitflow.worker.service.StepProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StepReadyConsumer {

    private final StepProcessingService stepProcessingService;
    private final StepCompletedProducer stepCompletedProducer;
    private final StepFailedProducer stepFailedProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${orbitflow.worker.instance-id}")
    private String instanceId;

    @KafkaListener(topics = "workflow.step.ready")
    public void onStepReady(String payload, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition)
        throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(payload);
        StepReadyEvent event = new StepReadyEvent(
            UUID.fromString(node.get("runId").asText()),
            UUID.fromString(node.get("stepId").asText()),
            node.get("stepName").asText());

        log.info("[{}] partition {} - received step-ready event for run {} step '{}'",
            instanceId, partition, event.runId(), event.stepName());

        StepProcessingResult result = stepProcessingService.process(event);
        JobStep step = result.step();
        if (step == null) {
            return;
        }

        // Published only now that process()'s transaction has committed.
        if (step.getStatus() == StepStatus.COMPLETED) {
            stepCompletedProducer.publish(new StepCompletedEvent(event.runId(), step.getId(), step.getStepName()));
        } else {
            stepFailedProducer.publish(
                new StepFailedEvent(event.runId(), step.getId(), step.getStepName(), result.errorMessage()));
        }
    }
}
