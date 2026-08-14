package com.orbitflow.api.dto.response;

import com.orbitflow.api.entity.StepStatus;

import java.util.Set;
import java.util.UUID;

public record StepStatusResponse(
    UUID stepId,
    String stepName,
    StepStatus status,
    Set<String> dependsOn,
    int attemptCount
) {
}
