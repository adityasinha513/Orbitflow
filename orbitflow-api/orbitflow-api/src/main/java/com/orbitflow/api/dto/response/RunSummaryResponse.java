package com.orbitflow.api.dto.response;

import com.orbitflow.api.entity.RunStatus;

import java.time.Instant;
import java.util.UUID;

public record RunSummaryResponse(
    UUID runId,
    String workflowName,
    RunStatus status,
    String submittedBy,
    Instant startedAt,
    Instant completedAt
) {
}
