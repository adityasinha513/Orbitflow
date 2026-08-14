package com.orbitflow.worker.service;

import com.orbitflow.worker.entity.JobStep;
import com.orbitflow.worker.entity.StepStatus;
import com.orbitflow.worker.event.StepDlqEvent;
import com.orbitflow.worker.event.StepFailedEvent;
import com.orbitflow.worker.event.StepReadyEvent;
import com.orbitflow.worker.repository.JobStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decides retry-vs-DLQ and writes the resulting status, but does not publish or schedule
 * anything itself - the caller (StepFailedConsumer) acts on the returned decision only after
 * this transaction has committed, so a fast redelivery can never observe an uncommitted status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private final JobStepRepository jobStepRepository;

    @Value("${orbitflow.worker.retry.max-attempts:5}")
    private int maxAttempts;

    @Value("${orbitflow.worker.retry.initial-backoff-ms:1000}")
    private long initialBackoffMs;

    @Value("${orbitflow.worker.retry.backoff-multiplier:2.0}")
    private double backoffMultiplier;

    @Transactional
    public RetryDecision handleFailure(StepFailedEvent event) {
        JobStep step = jobStepRepository.findById(event.stepId()).orElse(null);
        if (step == null) {
            log.warn("step {} not found, dropping failed event", event.stepId());
            return RetryDecision.noOp();
        }

        // Guards against reprocessing if step.failed is redelivered after the retry/DLQ
        // decision has already been made (step has since moved on to READY or DEAD_LETTER).
        if (step.getStatus() != StepStatus.FAILED) {
            log.info("step '{}' is {}, not FAILED, skipping duplicate failure event", step.getStepName(), step.getStatus());
            return RetryDecision.noOp();
        }

        if (step.getAttemptCount() >= maxAttempts) {
            step.setStatus(StepStatus.DEAD_LETTER);
            jobStepRepository.save(step);
            log.warn("step '{}' exhausted {} attempts, moved to DLQ", step.getStepName(), step.getAttemptCount());
            return RetryDecision.deadLetter(
                new StepDlqEvent(event.runId(), step.getId(), step.getStepName(), event.errorMessage()));
        }

        step.setStatus(StepStatus.READY);
        jobStepRepository.save(step);

        long delayMs = (long) (initialBackoffMs * Math.pow(backoffMultiplier, step.getAttemptCount() - 1));
        log.info("step '{}' failed attempt {}/{}, retrying in {}ms",
            step.getStepName(), step.getAttemptCount(), maxAttempts, delayMs);

        return RetryDecision.retry(new StepReadyEvent(event.runId(), step.getId(), step.getStepName()), delayMs);
    }
}
