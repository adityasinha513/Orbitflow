package com.orbitflow.worker.entity;

public enum StepStatus {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED,
    DEAD_LETTER
}
