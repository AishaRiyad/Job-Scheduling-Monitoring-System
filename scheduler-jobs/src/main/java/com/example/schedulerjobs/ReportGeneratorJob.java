package com.example.schedulerjobs;

import com.example.schedulercore.Job;

import java.util.concurrent.atomic.AtomicInteger;

public class ReportGeneratorJob implements Job {
    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public void run(Job.JobContext ctx) throws Exception {
        int n = counter.incrementAndGet();
        if (n % 2 == 1) {
            throw new RuntimeException("Simulated failure on attempt " + n);
        }
        Thread.sleep(400);
        System.out.println("[ReportGenerator] Success on attempt " + n);
    }
}
