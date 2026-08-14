package com.orbitflow.worker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Trimmed view of job_runs: orbitflow-api owns the full row; the worker only
 * needs the input payload to hand to a step's webhook callout.
 */
@Entity
@Table(name = "job_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobRun {

    @Id
    private UUID id;

    @Lob
    @Column(name = "input_payload")
    private String inputPayload;
}
