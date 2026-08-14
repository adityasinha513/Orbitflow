package com.orbitflow.api.dto.response;

import java.util.Set;

public record StepDefinitionResponse(
    String name,
    String callbackUrl,
    Set<String> dependsOn
) {
}
