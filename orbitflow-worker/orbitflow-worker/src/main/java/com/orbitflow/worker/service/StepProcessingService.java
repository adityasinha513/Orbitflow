package com.orbitflow.worker.service;

import com.orbitflow.worker.entity.ExecutionResult;
import com.orbitflow.worker.entity.JobRun;
import com.orbitflow.worker.entity.JobStep;
import com.orbitflow.worker.entity.StepExecutionLog;
import com.orbitflow.worker.entity.StepStatus;
import com.orbitflow.worker.event.StepReadyEvent;
import com.orbitflow.worker.repository.JobRunRepository;
import com.orbitflow.worker.repository.JobStepRepository;
import com.orbitflow.worker.repository.StepExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Does not publish anything itself - the caller (StepReadyConsumer) publishes based on the
 * returned result, only after this transaction has committed. A step.completed/step.failed
 * message published from inside this transaction could otherwise reach a consumer before the
 * status write it describes is actually visible, since the next hop can react in milliseconds.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StepProcessingService {

    private final JobStepRepository jobStepRepository;
    private final JobRunRepository jobRunRepository;
    private final StepExecutionLogRepository stepExecutionLogRepository;
    private final WebhookStepExecutor stepExecutor;

    @Transactional
    public StepProcessingResult process(StepReadyEvent event) {
        JobStep step = jobStepRepository.findById(event.stepId()).orElse(null);
        if (step == null) {
            log.warn("step {} not found, dropping event", event.stepId());
            return StepProcessingResult.skipped();
        }

        // Guards against the same step.ready delivery being processed twice (at-least-once Kafka delivery).
        if (step.getStatus() != StepStatus.READY) {
            log.info("step '{}' already {}, skipping duplicate delivery", step.getStepName(), step.getStatus());
            return StepProcessingResult.skipped();
        }

        JobRun jobRun = jobRunRepository.findById(step.getJobRunId()).orElse(null);
        String inputPayload = jobRun == null ? null : jobRun.getInputPayload();

        step.setStatus(StepStatus.RUNNING);
        int attempt = step.getAttemptCount() + 1;
        step.setAttemptCount(attempt);
        jobStepRepository.save(step);

        Instant startedAt = Instant.now();
        StepExecutionOutcome outcome = stepExecutor.execute(step.getCallbackUrl(), inputPayload);
        Instant finishedAt = Instant.now();

        stepExecutionLogRepository.save(StepExecutionLog.builder()
            .jobStep(step)
            .attemptNumber(attempt)
            .result(outcome.result())
            .startedAt(startedAt)
            .finishedAt(finishedAt)
            .errorMessage(outcome.errorMessage())
            .build());

        boolean succeeded = outcome.result() == ExecutionResult.SUCCESS;
        step.setStatus(succeeded ? StepStatus.COMPLETED : StepStatus.FAILED);
        jobStepRepository.save(step);

        log.info("step '{}' finished with {}", step.getStepName(), step.getStatus());
        return new StepProcessingResult(step, outcome.errorMessage());
    }
}
