package com.example.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

class TestUtils {

    static void quietSleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    static boolean waitUntilTrue(BooleanSupplier condition, long timeoutMs) {
        long start = System.nanoTime();
        while ((System.nanoTime() - start) / 1_000_000L < timeoutMs) {
            if (condition.getAsBoolean()) return true;
            quietSleep(20);
        }
        return condition.getAsBoolean();
    }

    static class CountingJob implements Job {
        private final AtomicInteger counter;
        private final long workMillis;

        CountingJob(AtomicInteger counter, long workMillis) {
            this.counter = counter;
            this.workMillis = workMillis;
        }

        @Override
        public void run() throws Exception {
            counter.incrementAndGet();
            if (workMillis > 0) Thread.sleep(workMillis);
        }
    }

    static class FlakyJob implements Job {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private final int failTimes;
        private final long workMillis;

        FlakyJob(int failTimes, long workMillis) {
            this.failTimes = Math.max(0, failTimes);
            this.workMillis = workMillis;
        }

        @Override
        public void run() throws Exception {
            int n = attempts.incrementAndGet();
            if (n <= failTimes) throw new RuntimeException("Planned failure #" + n);
            if (workMillis > 0) Thread.sleep(workMillis);
        }
    }
}
