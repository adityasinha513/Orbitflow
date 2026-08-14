package com.orbitflow.api.repository;

import com.orbitflow.api.entity.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {
}
