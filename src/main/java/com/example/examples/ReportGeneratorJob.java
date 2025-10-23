package com.example.examples;

import com.example.scheduler.Job;

import java.util.concurrent.ThreadLocalRandom;

public class ReportGeneratorJob implements Job {
    @Override
    public void run() throws Exception {
        Thread.sleep(400);
        if (ThreadLocalRandom.current().nextInt(0, 3) == 0) {
            throw new RuntimeException("Data source unavailable");
        }
    }
}
