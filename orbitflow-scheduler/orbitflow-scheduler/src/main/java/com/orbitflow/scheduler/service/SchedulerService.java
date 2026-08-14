package com.orbitflow.scheduler.service;

import com.orbitflow.scheduler.entity.JobRun;
import com.orbitflow.scheduler.entity.JobStep;
import com.orbitflow.scheduler.entity.RunStatus;
import com.orbitflow.scheduler.event.StepReadyEvent;
import com.orbitflow.scheduler.kafka.producer.StepReadyProducer;
import com.orbitflow.scheduler.repository.JobRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final DagResolutionService dagResolutionService;
    private final JobRunRepository jobRunRepository;
    private final StepReadyProducer stepReadyProducer;
    private final RedissonClient redissonClient;

    @Value("${orbitflow.scheduler.instance-id}")
    private String instanceId;

    @Value("${orbitflow.scheduler.lock.wait-time-ms:0}")
    private long lockWaitTimeMs;

    @Value("${orbitflow.scheduler.lock.lease-time-ms:10000}")
    private long lockLeaseTimeMs;

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
     * A per-run Redis lock so that with multiple scheduler instances running, only one of
     * them ever resolves/dispatches for a given job at a time - across both the poller and
     * the reactive step-outcome listener, on any instance. A zero wait time means an instance
     * that loses the race just skips this run this tick rather than queueing behind the winner;
     * it'll pick it up on the next poll or the next outcome event. The lease auto-expires so a
     * crashed instance can't leave a run permanently locked.
     */
    public void resolveRun(UUID runId) {
        RLock lock = redissonClient.getLock("orbitflow:lock:run:" + runId);
        boolean acquired;
        try {
            acquired = lock.tryLock(lockWaitTimeMs, lockLeaseTimeMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!acquired) {
            log.debug("[{}] run {} is locked by another scheduler instance, skipping this tick", instanceId, runId);
            return;
        }

        try {
            for (JobStep step : dagResolutionService.markNewlyReadySteps(runId)) {
                log.info("[{}] dispatching step '{}' for run {}", instanceId, step.getStepName(), runId);
                stepReadyProducer.publish(new StepReadyEvent(runId, step.getId(), step.getStepName()));
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
