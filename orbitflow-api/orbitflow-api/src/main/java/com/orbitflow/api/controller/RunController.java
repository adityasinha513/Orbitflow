package com.orbitflow.api.controller;

import com.orbitflow.api.dto.request.SubmitRunRequest;
import com.orbitflow.api.dto.response.RunResponse;
import com.orbitflow.api.dto.response.RunSummaryResponse;
import com.orbitflow.api.dto.response.StepStatusResponse;
import com.orbitflow.api.entity.JobRun;
import com.orbitflow.api.entity.RunStatus;
import com.orbitflow.api.service.RateLimitService;
import com.orbitflow.api.service.RunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;
    private final RateLimitService rateLimitService;

    @PostMapping("/workflows/{workflowName}/runs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RunResponse submitRun(@PathVariable String workflowName,
                                  @RequestBody(required = false) SubmitRunRequest request,
                                  Authentication authentication) {
        rateLimitService.checkRunSubmission(authentication.getName());
        SubmitRunRequest effectiveRequest = request == null ? new SubmitRunRequest(Map.of()) : request;
        JobRun run = runService.submitRun(workflowName, effectiveRequest, authentication.getName());
        return toResponse(run);
    }

    @GetMapping("/runs/{runId}")
    public RunResponse getRun(@PathVariable UUID runId) {
        JobRun run = runService.getRun(runId);
        return toResponse(run);
    }

    @GetMapping("/runs")
    public List<RunSummaryResponse> listRuns(@RequestParam(required = false) String workflow,
                                              @RequestParam(required = false) RunStatus status,
                                              @RequestParam(defaultValue = "20") int limit) {
        return runService.listRuns(workflow, status, limit);
    }

    private RunResponse toResponse(JobRun run) {
        return new RunResponse(
            run.getId(),
            run.getWorkflow().getName(),
            run.getStatus(),
            run.getSubmittedBy(),
            run.getStartedAt(),
            run.getCompletedAt(),
            run.getSteps().stream()
                .map(s -> new StepStatusResponse(s.getId(), s.getStepName(), s.getStatus(), s.getDependsOn(), s.getAttemptCount()))
                .toList()
        );
    }
}
