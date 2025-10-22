package com.example.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerIntegrationTest {

    private Scheduler scheduler;

    @AfterEach
    void tearDown() {
        if (scheduler != null) scheduler.close();
    }

    @Test
    void parallelExecution_monitoring_reporting() {
        scheduler = new Scheduler(4);

        AtomicInteger backupCount = new AtomicInteger(0);
        AtomicInteger cleanupCount = new AtomicInteger(0);
        Set<String> threads = new ConcurrentSkipListSet<>();

        Job backup = () -> {
            threads.add(Thread.currentThread().getName());
            new TestUtils.CountingJob(backupCount, 60).run();
        };
        Job cleanup = () -> {
            threads.add(Thread.currentThread().getName());
            new TestUtils.CountingJob(cleanupCount, 60).run();
        };

        JobScheduling j1 = new JobScheduling("DataBackupJob", backup, 120L, 0, 0);
        JobScheduling j2 = new JobScheduling("LogCleanupJob", cleanup, 150L, 0, 0);

        scheduler.getJobRegistration().registerJob(j1);
        scheduler.getJobRegistration().registerJob(j2);
        scheduler.scheduleAllRegistered();

        boolean ok = TestUtils.waitUntilTrue(
                () -> backupCount.get() >= 3 && cleanupCount.get() >= 2, 5000);
        assertTrue(ok, "Expected multiple executions within timeout");

        assertTrue(threads.size() >= 2, "Expected parallelism across threads");

        var runningNow = scheduler.runningJobs();
        assertNotNull(runningNow);

        Map<String, JobRuntimeDetails> snapshot = scheduler.snapshotRuntime();
        assertTrue(snapshot.containsKey("DataBackupJob"));
        assertTrue(snapshot.containsKey("LogCleanupJob"));

        JobRuntimeDetails d1 = snapshot.get("DataBackupJob");
        assertTrue(d1.getLastEndTime() >= 0);
        assertTrue(d1.getLastDuration() >= 0);

        Reporting reporting = new Reporting(scheduler.getJobHistory());
        var lastHour = reporting.lastHour();
        assertFalse(lastHour.isEmpty(), "History should have entries");
        var byStatus = reporting.countByStatusLastHour();
        assertTrue(byStatus.values().stream().mapToLong(Long::longValue).sum() >= 1);
    }
}
