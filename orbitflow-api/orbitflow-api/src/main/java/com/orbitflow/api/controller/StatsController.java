package com.orbitflow.api.controller;

import com.orbitflow.api.dto.response.DashboardStatsResponse;
import com.orbitflow.api.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public DashboardStatsResponse getStats() {
        return statsService.getStats();
    }
}
