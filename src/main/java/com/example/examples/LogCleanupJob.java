package com.example.examples;

import com.example.scheduler.Job;

public class LogCleanupJob implements Job {
    @Override
    public void run() throws Exception {
        Thread.sleep(200);
    }
}
