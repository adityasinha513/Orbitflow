package com.orbitflow.worker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Trimmed view of job_steps: the worker owns execution, so unlike the scheduler's
 * view it maps callback_url/attempt_count/updated_at too.
 */
@Entity
@Table(name = "job_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobStep {

    @Id
    private UUID id;

    @Column(name = "job_run_id", nullable = false)
    private UUID jobRunId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
