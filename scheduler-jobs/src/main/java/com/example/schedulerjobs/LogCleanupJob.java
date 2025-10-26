package com.example.schedulerjobs;

import com.example.schedulercore.Job;

public class LogCleanupJob implements Job {
    @Override
    public void run(Job.JobContext ctx) throws Exception {
        Thread.sleep(800);
        System.out.println("[LogCleanup] Executed #" + ctx.getExecutionId());
    }
}
