package com.orbitflow.worker.repository;

import com.orbitflow.worker.entity.JobStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobStepRepository extends JpaRepository<JobStep, UUID> {
}
