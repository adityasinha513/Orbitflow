package com.orbitflow.worker.service;

import com.orbitflow.worker.entity.JobStep;

/** Null step means the event was dropped (step not found, or a duplicate delivery). */
public record StepProcessingResult(JobStep step, String errorMessage) {

    public static StepProcessingResult skipped() {
        return new StepProcessingResult(null, null);
    }
}
