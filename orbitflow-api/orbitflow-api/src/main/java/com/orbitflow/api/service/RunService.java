package com.orbitflow.api.service;

import com.orbitflow.api.dto.request.SubmitRunRequest;
import com.orbitflow.api.dto.response.RunSummaryResponse;
import com.orbitflow.api.entity.JobRun;
import com.orbitflow.api.entity.JobStep;
import com.orbitflow.api.entity.RunStatus;
import com.orbitflow.api.entity.StepStatus;
import com.orbitflow.api.entity.Workflow;
import com.orbitflow.api.exception.RunNotFoundException;
import com.orbitflow.api.repository.JobRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persists a run and its steps, all PENDING, and returns immediately.
 * orbitflow-scheduler is what notices the run and dispatches ready steps via Kafka.
 */
@Service
@RequiredArgsConstructor
public class RunService {

    private final WorkflowService workflowService;
    private final JobRunRepository jobRunRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public JobRun submitRun(String workflowName, SubmitRunRequest request, String submittedBy) {
        Workflow workflow = workflowService.getByName(workflowName);

        JobRun jobRun = JobRun.builder()
            .workflow(workflow)
            .status(RunStatus.RUNNING)
            .inputPayload(writeInput(request.input()))
            .submittedBy(submittedBy)
            .startedAt(Instant.now())
            .build();

        List<JobStep> steps = workflow.getSteps().stream()
            .map(def -> JobStep.builder()
                .jobRun(jobRun)
                .stepName(def.getStepName())
                .callbackUrl(def.getCallbackUrl())
                .dependsOn(new HashSet<>(def.getDependsOn()))
                .status(StepStatus.PENDING)
                .attemptCount(0)
                .build())
            .toList();
        jobRun.setSteps(steps);

        // save() persists this same managed instance in place (client-generated UUID ids), no reassignment needed.
        jobRunRepository.save(jobRun);

        return jobRun;
    }

    @Transactional(readOnly = true)
    public JobRun getRun(UUID runId) {
        return jobRunRepository.findById(runId)
            .orElseThrow(() -> new RunNotFoundException("run '%s' not found".formatted(runId)));
    }

    @Transactional(readOnly = true)
    public List<RunSummaryResponse> listRuns(String workflowName, RunStatus status, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        List<JobRun> runs;
        if (workflowName != null && status != null) {
            runs = jobRunRepository.findByWorkflow_NameAndStatusOrderByStartedAtDesc(workflowName, status, pageable);
        } else if (workflowName != null) {
            runs = jobRunRepository.findByWorkflow_NameOrderByStartedAtDesc(workflowName, pageable);
        } else if (status != null) {
            runs = jobRunRepository.findByStatusOrderByStartedAtDesc(status, pageable);
        } else {
            runs = jobRunRepository.findAllByOrderByStartedAtDesc(pageable);
        }

        return runs.stream()
            .map(run -> new RunSummaryResponse(
                run.getId(), run.getWorkflow().getName(), run.getStatus(), run.getSubmittedBy(),
                run.getStartedAt(), run.getCompletedAt()))
            .toList();
    }

    private String writeInput(Map<String, Object> input) {
        return objectMapper.writeValueAsString(input == null ? Map.of() : input);
    }
}
