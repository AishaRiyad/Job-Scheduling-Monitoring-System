package com.example.schedulercore;

import java.time.Duration;

public interface RetryPolicy {
    static RetryPolicy fixedAttempts(int maxAttempts, Duration delay) {
        return new RetryPolicy() {
            @Override
            public boolean shouldRetry(int attempt, Throwable lastError) {
                return attempt < maxAttempts;
            }

            @Override
            public Duration nextDelay(int attempt) {
                return delay;
            }
        };
    }

    static RetryPolicy exponentialBackoff(int maxAttempts, Duration base) {
        return new RetryPolicy() {
            @Override
            public boolean shouldRetry(int attempt, Throwable lastError) {
                return attempt < maxAttempts;
            }

            @Override
            public Duration nextDelay(int attempt) {
                long factor = 1L << Math.max(0, attempt);
                return base.multipliedBy(factor);
            }
        };
    }

    boolean shouldRetry(int attempt, Throwable lastError);

    Duration nextDelay(int attempt);
}
