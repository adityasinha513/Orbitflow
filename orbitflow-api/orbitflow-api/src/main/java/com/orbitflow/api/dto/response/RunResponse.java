package com.orbitflow.api.dto.response;

import com.orbitflow.api.entity.RunStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RunResponse(
    UUID runId,
    String workflowName,
    RunStatus status,
    Instant startedAt,
    Instant completedAt,
    List<StepStatusResponse> steps
) {
}
