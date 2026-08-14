package com.orbitflow.scheduler.entity;

public enum StepStatus {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED,
    DEAD_LETTER
}
