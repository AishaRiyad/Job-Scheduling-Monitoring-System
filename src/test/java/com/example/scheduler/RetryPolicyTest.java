package com.example.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    private Scheduler scheduler;

    @AfterEach
    void tearDown() {
        if (scheduler != null) scheduler.close();
    }

    @Test
    void flakyJob_retries_thenSuccess_orFail() {
        scheduler = new Scheduler(2);

        Job flaky = new TestUtils.FlakyJob(2, 10);
        JobScheduling cfg = new JobScheduling("ReportGeneratorJob", flaky, 100L, 3, 50L);
        scheduler.getJobRegistration().registerJob(cfg);
        scheduler.scheduleAllRegistered();

        boolean gotHistory = TestUtils.waitUntilTrue(
                () -> !scheduler.getJobHistory().byJobName("ReportGeneratorJob").isEmpty(), 5000);
        assertTrue(gotHistory, "No history produced for ReportGeneratorJob");

        var results = scheduler.getJobHistory().byJobName("ReportGeneratorJob");
        var last = results.get(results.size() - 1);

        assertTrue(last.getAttempt() >= 1);
        assertNotNull(last.getJobStatus());
    }

    @Test
    void flakyJob_exceedsRetries_fails() {
        scheduler = new Scheduler(2);

        Job tooFlaky = new TestUtils.FlakyJob(5, 0);
        JobScheduling cfg = new JobScheduling("TooFlakyJob", tooFlaky, 80L, 2, 20L);
        scheduler.getJobRegistration().registerJob(cfg);
        scheduler.scheduleAllRegistered();

        boolean gotHistory = TestUtils.waitUntilTrue(
                () -> !scheduler.getJobHistory().byJobName("TooFlakyJob").isEmpty(), 5000);
        assertTrue(gotHistory, "No history produced for TooFlakyJob");

        var results = scheduler.getJobHistory().byJobName("TooFlakyJob");
        var last = results.get(results.size() - 1);

        assertEquals(JobStatus.FAILED, last.getJobStatus());
        assertTrue(last.getAttempt() <= 2, "should not exceed maximum retries");
        assertNotNull(last.getErrorMessage());
    }
}
