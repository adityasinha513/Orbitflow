package com.orbitflow.worker.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitflow.worker.event.StepFailedEvent;
import com.orbitflow.worker.kafka.producer.StepDlqProducer;
import com.orbitflow.worker.kafka.producer.StepReadyProducer;
import com.orbitflow.worker.service.RetryDecision;
import com.orbitflow.worker.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StepFailedConsumer {

    private final RetryService retryService;
    private final StepReadyProducer stepReadyProducer;
    private final StepDlqProducer stepDlqProducer;
    private final TaskScheduler taskScheduler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "workflow.step.failed")
    public void onStepFailed(String payload) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(payload);
        StepFailedEvent event = new StepFailedEvent(
            UUID.fromString(node.get("runId").asText()),
            UUID.fromString(node.get("stepId").asText()),
            node.get("stepName").asText(),
            node.hasNonNull("errorMessage") ? node.get("errorMessage").asText() : null);

        log.info("step '{}' failed, evaluating retry/DLQ", event.stepName());

        // retryService.handleFailure()'s transaction has committed by the time we act below.
        RetryDecision decision = retryService.handleFailure(event);
        if (decision.dlqEvent() != null) {
            stepDlqProducer.publish(decision.dlqEvent());
        } else if (decision.retryEvent() != null) {
            taskScheduler.schedule(
                () -> stepReadyProducer.publish(decision.retryEvent()),
                Instant.now().plusMillis(decision.retryDelayMs()));
        }
    }
}
