package com.orbitflow.api.controller;

import com.orbitflow.api.dto.response.DlqEntryResponse;
import com.orbitflow.api.dto.response.StepStatusResponse;
import com.orbitflow.api.entity.JobStep;
import com.orbitflow.api.service.DlqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dlq")
@RequiredArgsConstructor
public class DlqController {

    private final DlqService dlqService;

    @GetMapping
    public List<DlqEntryResponse> listDeadLettered() {
        return dlqService.listDeadLettered();
    }

    @PostMapping("/{stepId}/replay")
    public StepStatusResponse replay(@PathVariable UUID stepId) {
        JobStep step = dlqService.replay(stepId);
        return new StepStatusResponse(step.getId(), step.getStepName(), step.getStatus(), step.getDependsOn(), step.getAttemptCount());
    }
}
