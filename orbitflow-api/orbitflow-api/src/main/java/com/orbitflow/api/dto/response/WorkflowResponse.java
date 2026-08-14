package com.orbitflow.api.dto.response;

import java.time.Instant;
import java.util.List;

public record WorkflowResponse(
    Long id,
    String name,
    Instant createdAt,
    List<StepDefinitionResponse> steps
) {
}
