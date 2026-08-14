package com.orbitflow.scheduler.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Trimmed view of job_steps: orbitflow-api owns callback_url/attempt_count/timestamps;
 * the scheduler only needs step_name, status and its dependency set to resolve the DAG.
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "job_step_dependencies",
        joinColumns = @JoinColumn(name = "job_step_id")
    )
    @Column(name = "depends_on_step_name")
    private Set<String> dependsOn = new HashSet<>();
}
