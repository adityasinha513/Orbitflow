package com.orbitflow.worker.event;

import java.util.UUID;

public record StepCompletedEvent(UUID runId, UUID stepId, String stepName) {
}
