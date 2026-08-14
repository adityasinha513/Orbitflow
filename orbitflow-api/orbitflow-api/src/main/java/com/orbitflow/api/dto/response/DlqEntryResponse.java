package com.orbitflow.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DlqEntryResponse(
    UUID stepId,
    String stepName,
    UUID runId,
    String workflowName,
    String failureReason,
    Instant failedAt,
    int attempts
) {
}
