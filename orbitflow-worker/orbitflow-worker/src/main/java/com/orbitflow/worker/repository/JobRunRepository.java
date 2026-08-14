package com.orbitflow.worker.repository;

import com.orbitflow.worker.entity.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {
}
