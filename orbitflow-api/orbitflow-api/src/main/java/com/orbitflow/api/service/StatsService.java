package com.orbitflow.api.service;

import com.orbitflow.api.dto.response.DashboardStatsResponse;
import com.orbitflow.api.entity.ExecutionResult;
import com.orbitflow.api.entity.RunStatus;
import com.orbitflow.api.entity.StepExecutionLog;
import com.orbitflow.api.entity.StepStatus;
import com.orbitflow.api.repository.JobRunRepository;
import com.orbitflow.api.repository.JobStepRepository;
import com.orbitflow.api.repository.StepExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final int DURATION_SAMPLE_SIZE = 100;

    private final JobRunRepository jobRunRepository;
    private final JobStepRepository jobStepRepository;
    private final StepExecutionLogRepository stepExecutionLogRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);

        long activeRuns = jobRunRepository.countByStatus(RunStatus.RUNNING);
        long completedToday = jobRunRepository.countByStatusAndCompletedAtAfter(RunStatus.COMPLETED, startOfToday);
        long inDeadLetterQueue = jobStepRepository.countByStatus(StepStatus.DEAD_LETTER);
        Long avgStepDurationMs = averageRecentStepDurationMs();

        return new DashboardStatsResponse(activeRuns, completedToday, inDeadLetterQueue, avgStepDurationMs);
    }

    private Long averageRecentStepDurationMs() {
        List<StepExecutionLog> recentSuccesses = stepExecutionLogRepository
            .findByResultOrderByFinishedAtDesc(ExecutionResult.SUCCESS, PageRequest.of(0, DURATION_SAMPLE_SIZE));

        if (recentSuccesses.isEmpty()) {
            return null;
        }

        return (long) recentSuccesses.stream()
            .mapToLong(log -> Duration.between(log.getStartedAt(), log.getFinishedAt()).toMillis())
            .average()
            .orElse(0);
    }
}
