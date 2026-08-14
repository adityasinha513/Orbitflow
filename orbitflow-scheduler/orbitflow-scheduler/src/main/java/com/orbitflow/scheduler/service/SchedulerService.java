package com.orbitflow.scheduler.service;

import com.orbitflow.scheduler.entity.JobRun;
import com.orbitflow.scheduler.entity.JobStep;
import com.orbitflow.scheduler.entity.RunStatus;
import com.orbitflow.scheduler.event.StepReadyEvent;
import com.orbitflow.scheduler.kafka.producer.StepReadyProducer;
import com.orbitflow.scheduler.repository.JobRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final DagResolutionService dagResolutionService;
    private final JobRunRepository jobRunRepository;
    private final StepReadyProducer stepReadyProducer;

    /**
     * Catches runs the reactive path won't: a freshly-submitted run has no
     * step.completed event to react to yet, so something has to pick up its
     * initial zero-dependency steps.
     */
    @Scheduled(fixedDelayString = "${orbitflow.scheduler.poll-interval-ms:2000}")
    public void pollRunningJobs() {
        for (JobRun run : jobRunRepository.findByStatus(RunStatus.RUNNING)) {
            resolveRun(run.getId());
        }
    }

    /**
     * Single instance only in this phase, so a JVM-local lock is enough to stop the
     * poller and the step-outcome listener racing on the same run. Phase 5 replaces
     * this with a per-job Redis lock once multiple scheduler instances are in play.
     */
    public synchronized void resolveRun(UUID runId) {
        for (JobStep step : dagResolutionService.markNewlyReadySteps(runId)) {
            log.info("dispatching step '{}' for run {}", step.getStepName(), runId);
            stepReadyProducer.publish(new StepReadyEvent(runId, step.getId(), step.getStepName()));
        }
    }
}
