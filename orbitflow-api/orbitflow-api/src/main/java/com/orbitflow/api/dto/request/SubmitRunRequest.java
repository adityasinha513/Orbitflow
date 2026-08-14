package com.orbitflow.api.dto.request;

import java.util.Map;

public record SubmitRunRequest(
    Map<String, Object> input
) {
}
