package com.orbitflow.worker.service;

import com.orbitflow.worker.event.StepDlqEvent;
import com.orbitflow.worker.event.StepReadyEvent;

/** Exactly one of retryEvent or dlqEvent is set; both null means the failure event was a no-op (duplicate/not found). */
public record RetryDecision(StepReadyEvent retryEvent, long retryDelayMs, StepDlqEvent dlqEvent) {

    public static RetryDecision retry(StepReadyEvent event, long delayMs) {
        return new RetryDecision(event, delayMs, null);
    }

    public static RetryDecision deadLetter(StepDlqEvent event) {
        return new RetryDecision(null, 0, event);
    }

    public static RetryDecision noOp() {
        return new RetryDecision(null, 0, null);
    }
}
