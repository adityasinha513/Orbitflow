package com.orbitflow.worker.event;

import java.util.UUID;

public record StepReadyEvent(UUID runId, UUID stepId, String stepName) {
}
