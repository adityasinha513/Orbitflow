package com.orbitflow.scheduler.service;

import com.orbitflow.scheduler.entity.JobRun;
import com.orbitflow.scheduler.entity.JobStep;
import com.orbitflow.scheduler.entity.RunStatus;
import com.orbitflow.scheduler.entity.StepStatus;
import com.orbitflow.scheduler.repository.JobRunRepository;
import com.orbitflow.scheduler.repository.JobStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Pure DB-mutating DAG resolution, kept on its own bean (not SchedulerService) so its
 * @Transactional boundary is a genuine cross-bean call and always honored - including from
 * the poller, which would otherwise self-invoke a method on its own class and silently skip
 * transaction handling. Callers publish Kafka events themselves, only after this returns, so
 * a fast consumer can never observe a status write that isn't committed yet.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DagResolutionService {

    private final JobRunRepository jobRunRepository;
    private final JobStepRepository jobStepRepository;

    @Transactional
    public List<JobStep> markNewlyReadySteps(UUID runId) {
        JobRun run = jobRunRepository.findById(runId).orElse(null);
        if (run == null || run.getStatus() != RunStatus.RUNNING) {
            return List.of();
        }

        List<JobStep> steps = jobStepRepository.findByJobRunId(runId);

        Set<String> completedNames = steps.stream()
            .filter(s -> s.getStatus() == StepStatus.COMPLETED)
            .map(JobStep::getStepName)
            .collect(Collectors.toSet());

        List<JobStep> newlyReady = steps.stream()
            .filter(s -> s.getStatus() == StepStatus.PENDING)
            .filter(s -> completedNames.containsAll(s.getDependsOn()))
            .toList();

        for (JobStep step : newlyReady) {
            step.setStatus(StepStatus.READY);
            jobStepRepository.save(step);
        }

        if (newlyReady.isEmpty()) {
            finalizeIfSettled(run, steps);
        }

        return newlyReady;
    }

    private void finalizeIfSettled(JobRun run, List<JobStep> steps) {
        // FAILED is transient here - the worker's retry handler hasn't decided yet whether
        // it'll be retried (back to READY) or dead-lettered, so it still counts as in-flight.
        boolean anyInFlight = steps.stream()
            .anyMatch(s -> s.getStatus() == StepStatus.READY
                || s.getStatus() == StepStatus.RUNNING
                || s.getStatus() == StepStatus.FAILED);
        if (anyInFlight) {
            return;
        }

        boolean anyDeadLettered = steps.stream().anyMatch(s -> s.getStatus() == StepStatus.DEAD_LETTER);
        run.setStatus(anyDeadLettered ? RunStatus.FAILED : RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());
        jobRunRepository.save(run);
        log.info("run {} settled as {}", run.getId(), run.getStatus());
    }
}
