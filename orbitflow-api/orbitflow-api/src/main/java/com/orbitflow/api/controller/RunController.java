package com.orbitflow.api.controller;

import com.orbitflow.api.dto.request.SubmitRunRequest;
import com.orbitflow.api.dto.response.RunResponse;
import com.orbitflow.api.dto.response.StepStatusResponse;
import com.orbitflow.api.entity.JobRun;
import com.orbitflow.api.service.RunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;

    @PostMapping("/workflows/{workflowName}/runs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RunResponse submitRun(@PathVariable String workflowName,
                                  @RequestBody(required = false) SubmitRunRequest request) {
        SubmitRunRequest effectiveRequest = request == null ? new SubmitRunRequest(Map.of()) : request;
        JobRun run = runService.submitRun(workflowName, effectiveRequest);
        return toResponse(run);
    }

    @GetMapping("/runs/{runId}")
    public RunResponse getRun(@PathVariable UUID runId) {
        JobRun run = runService.getRun(runId);
        return toResponse(run);
    }

    private RunResponse toResponse(JobRun run) {
        return new RunResponse(
            run.getId(),
            run.getWorkflow().getName(),
            run.getStatus(),
            run.getStartedAt(),
            run.getCompletedAt(),
            run.getSteps().stream()
                .map(s -> new StepStatusResponse(s.getId(), s.getStepName(), s.getStatus(), s.getDependsOn(), s.getAttemptCount()))
                .toList()
        );
    }
}
