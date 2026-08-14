package com.orbitflow.scheduler.repository;

import com.orbitflow.scheduler.entity.JobRun;
import com.orbitflow.scheduler.entity.RunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {

    List<JobRun> findByStatus(RunStatus status);
}
