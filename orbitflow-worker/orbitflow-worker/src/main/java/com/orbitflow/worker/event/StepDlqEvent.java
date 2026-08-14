package com.orbitflow.worker.event;

import java.util.UUID;

public record StepDlqEvent(UUID runId, UUID stepId, String stepName, String errorMessage) {
}
