package com.orbitflow.api.dto.response;

import java.time.Instant;
import java.util.List;

public record WorkflowSummaryResponse(
    Long id,
    String name,
    Instant createdAt,
    List<String> stepNames,
    RunSummaryResponse lastRun
) {
}
