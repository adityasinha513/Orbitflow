package com.orbitflow.api.repository;

import com.orbitflow.api.entity.JobStep;
import com.orbitflow.api.entity.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobStepRepository extends JpaRepository<JobStep, UUID> {

    long countByStatus(StepStatus status);

    List<JobStep> findByStatusOrderByUpdatedAtDesc(StepStatus status);
}
