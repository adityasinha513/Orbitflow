package com.orbitflow.worker.event;

import java.util.UUID;

public record StepFailedEvent(UUID runId, UUID stepId, String stepName, String errorMessage) {
}
