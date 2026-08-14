package com.orbitflow.api.repository;

import com.orbitflow.api.entity.JobStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobStepRepository extends JpaRepository<JobStep, UUID> {
}
