package com.orbitflow.api.service;

import com.orbitflow.api.dto.response.DlqEntryResponse;
import com.orbitflow.api.entity.JobRun;
import com.orbitflow.api.entity.JobStep;
import com.orbitflow.api.entity.RunStatus;
import com.orbitflow.api.entity.StepExecutionLog;
import com.orbitflow.api.entity.StepStatus;
import com.orbitflow.api.exception.InvalidStepStateException;
import com.orbitflow.api.exception.StepNotFoundException;
import com.orbitflow.api.repository.JobRunRepository;
import com.orbitflow.api.repository.JobStepRepository;
import com.orbitflow.api.repository.StepExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DlqService {

    private final JobStepRepository jobStepRepository;
    private final JobRunRepository jobRunRepository;
    private final StepExecutionLogRepository stepExecutionLogRepository;

    @Transactional
    public JobStep replay(UUID stepId) {
        JobStep step = jobStepRepository.findById(stepId)
            .orElseThrow(() -> new StepNotFoundException("step '%s' not found".formatted(stepId)));

        if (step.getStatus() != StepStatus.DEAD_LETTER) {
            throw new InvalidStepStateException(
                "step '%s' is %s, not dead-lettered".formatted(step.getStepName(), step.getStatus()));
        }

        step.setStatus(StepStatus.PENDING);
        step.setAttemptCount(0);
        jobStepRepository.save(step);

        // Its dependencies already completed once, so PENDING is enough - the scheduler's
        // poll will pick it straight back up as READY without needing dependency re-checks here.
        JobRun run = step.getJobRun();
        run.setStatus(RunStatus.RUNNING);
        run.setCompletedAt(null);
        jobRunRepository.save(run);

        return step;
    }

    @Transactional(readOnly = true)
    public List<DlqEntryResponse> listDeadLettered() {
        return jobStepRepository.findByStatusOrderByUpdatedAtDesc(StepStatus.DEAD_LETTER).stream()
            .map(this::toDlqEntry)
            .toList();
    }

    private DlqEntryResponse toDlqEntry(JobStep step) {
        StepExecutionLog lastAttempt = stepExecutionLogRepository
            .findTopByJobStep_IdOrderByAttemptNumberDesc(step.getId())
            .orElse(null);

        return new DlqEntryResponse(
            step.getId(),
            step.getStepName(),
            step.getJobRun().getId(),
            step.getJobRun().getWorkflow().getName(),
            lastAttempt == null ? null : lastAttempt.getErrorMessage(),
            lastAttempt == null ? step.getUpdatedAt() : lastAttempt.getFinishedAt(),
            step.getAttemptCount()
        );
    }
}
