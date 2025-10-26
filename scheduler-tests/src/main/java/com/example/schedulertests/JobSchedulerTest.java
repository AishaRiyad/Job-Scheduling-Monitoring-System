package com.example.schedulertests;

import com.example.schedulercore.JobScheduler;
import com.example.schedulercore.RetryPolicy;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertTrue;

public class JobSchedulerTest {
    @Test
    public void runsConcurrentlyAndStoresHistory() throws Exception {
        try (JobScheduler s = new JobScheduler()) {
            s.register("A", Duration.ofMillis(200), ctx -> Thread.sleep(300), null);
            s.register("B", Duration.ofMillis(200), ctx -> Thread.sleep(300), null);
            s.start();
            Thread.sleep(1200);
            s.stop();
            assertTrue(s.getHistory().size() >= 2);
        }
    }

    @Test
    public void retryPolicyApplies() throws Exception {
        try (JobScheduler s = new JobScheduler()) {
            s.register("Failing", Duration.ofMillis(200),
                    ctx -> {
                        throw new RuntimeException("boom");
                    },
                    RetryPolicy.fixedAttempts(2, Duration.ofMillis(50)));
            s.start();
            Thread.sleep(700);
            s.stop();
            assertTrue(s.getHistory().stream().anyMatch(r -> r.getJobName().equals("Failing")));
        }
    }
}
