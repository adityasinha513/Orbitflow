package com.orbitflow.worker.service;

import com.orbitflow.worker.entity.ExecutionResult;

public record StepExecutionOutcome(ExecutionResult result, String errorMessage) {

    public static StepExecutionOutcome success() {
        return new StepExecutionOutcome(ExecutionResult.SUCCESS, null);
    }

    public static StepExecutionOutcome failure(String errorMessage) {
        return new StepExecutionOutcome(ExecutionResult.FAILURE, errorMessage);
    }
}
