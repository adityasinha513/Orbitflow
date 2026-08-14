package com.orbitflow.scheduler.repository;

import com.orbitflow.scheduler.entity.JobStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobStepRepository extends JpaRepository<JobStep, UUID> {

    List<JobStep> findByJobRunId(UUID jobRunId);
}
