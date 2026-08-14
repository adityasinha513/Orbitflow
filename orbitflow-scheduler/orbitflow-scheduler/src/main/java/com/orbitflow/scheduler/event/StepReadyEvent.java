package com.orbitflow.scheduler.event;

import java.util.UUID;

public record StepReadyEvent(UUID runId, UUID stepId, String stepName) {
}
