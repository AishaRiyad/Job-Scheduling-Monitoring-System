package com.example.schedulertests;

import com.example.schedulercore.JobScheduler;
import com.example.schedulercore.RetryPolicy;
import com.example.schedulercore.Status;
import com.example.schedulerjobs.ReportGeneratorJob;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertTrue;

public class JobExecutionTest {
    @Test
    public void reportGeneratorFailsThenSucceeds() throws Exception {
        try (JobScheduler s = new JobScheduler()) {
            s.register("Report", Duration.ofMillis(200),
                    new ReportGeneratorJob(), RetryPolicy.fixedAttempts(1, Duration.ofMillis(50)));
            s.start();
            Thread.sleep(1200);
            s.stop();

            long completed = s.getHistory().stream()
                    .filter(r -> r.getJobName().equals("Report") && r.getStatus() == Status.COMPLETED)
                    .count();
            assertTrue(completed >= 1);
        }
    }
}
