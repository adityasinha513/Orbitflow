package com.orbitflow.api.dto.response;

public record DashboardStatsResponse(
    long activeRuns,
    long completedToday,
    long inDeadLetterQueue,
    Long avgStepDurationMs
) {
}
