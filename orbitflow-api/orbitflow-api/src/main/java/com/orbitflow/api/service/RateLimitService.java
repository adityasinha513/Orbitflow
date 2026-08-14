package com.orbitflow.api.service;

import com.orbitflow.api.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Scoped specifically to run submission, per the brief: "one client submitting a flood of runs can't starve everyone else." */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedissonClient redissonClient;

    @Value("${orbitflow.rate-limit.runs-per-window:20}")
    private long permitsPerWindow;

    @Value("${orbitflow.rate-limit.window-seconds:10}")
    private long windowSeconds;

    public void checkRunSubmission(String tenantKey) {
        RRateLimiter limiter = redissonClient.getRateLimiter("orbitflow:ratelimit:runs:" + tenantKey);
        limiter.trySetRate(RateType.OVERALL, permitsPerWindow, windowSeconds, RateIntervalUnit.SECONDS);
        if (!limiter.tryAcquire()) {
            throw new RateLimitExceededException(
                "rate limit exceeded for '%s': max %d run submissions per %ds".formatted(tenantKey, permitsPerWindow, windowSeconds));
        }
    }
}
