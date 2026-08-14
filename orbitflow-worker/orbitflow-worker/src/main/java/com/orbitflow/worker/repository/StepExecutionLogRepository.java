package com.orbitflow.worker.repository;

import com.orbitflow.worker.entity.StepExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepExecutionLogRepository extends JpaRepository<StepExecutionLog, Long> {
}
