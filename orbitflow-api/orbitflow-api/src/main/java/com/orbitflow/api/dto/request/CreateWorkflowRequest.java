package com.orbitflow.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CreateWorkflowRequest(
    @Pattern(regexp = "^[a-z0-9-]+$", message = "must be lowercase alphanumeric with hyphens only") String name,
    @NotEmpty(message = "must declare at least one step") @Valid List<StepDefinitionRequest> steps
) {
}
