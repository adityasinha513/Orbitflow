package com.orbitflow.api.repository;

import com.orbitflow.api.entity.JobRun;
import com.orbitflow.api.entity.RunStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {

    long countByStatus(RunStatus status);

    long countByStatusAndCompletedAtAfter(RunStatus status, Instant after);

    Optional<JobRun> findTopByWorkflowIdOrderByStartedAtDesc(Long workflowId);

    List<JobRun> findAllByOrderByStartedAtDesc(Pageable pageable);

    List<JobRun> findByWorkflow_NameOrderByStartedAtDesc(String workflowName, Pageable pageable);

    List<JobRun> findByStatusOrderByStartedAtDesc(RunStatus status, Pageable pageable);

    List<JobRun> findByWorkflow_NameAndStatusOrderByStartedAtDesc(String workflowName, RunStatus status, Pageable pageable);
}
