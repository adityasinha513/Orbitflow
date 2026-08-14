package com.orbitflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record StepDefinitionRequest(
    @NotBlank(message = "must not be blank") String name,
    @NotBlank(message = "must not be blank") String callbackUrl,
    List<String> dependsOn
) {
}
