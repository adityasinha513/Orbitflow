package com.orbitflow.scheduler.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitflow.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Both workflow.step.completed and workflow.step.dlq carry a runId and trigger the same
 * reaction here (re-evaluate the run), so one listener covers both rather than one class
 * per topic. Payload is parsed manually instead of via a typed JsonDeserializer since the
 * two topics don't share a payload shape and there's no shared event-class module to bind to.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StepOutcomeConsumer {

    private final SchedulerService schedulerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"workflow.step.completed", "workflow.step.dlq"})
    public void onStepOutcome(String payload) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(payload);
        UUID runId = UUID.fromString(node.get("runId").asText());
        log.info("step outcome event for run {}, re-checking downstream steps", runId);
        schedulerService.resolveRun(runId);
    }
}
