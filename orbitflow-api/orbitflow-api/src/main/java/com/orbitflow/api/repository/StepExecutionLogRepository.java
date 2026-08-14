package com.orbitflow.api.repository;

import com.orbitflow.api.entity.ExecutionResult;
import com.orbitflow.api.entity.StepExecutionLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StepExecutionLogRepository extends JpaRepository<StepExecutionLog, Long> {

    Optional<StepExecutionLog> findTopByJobStep_IdOrderByAttemptNumberDesc(UUID jobStepId);

    List<StepExecutionLog> findByResultOrderByFinishedAtDesc(ExecutionResult result, Pageable pageable);
}
