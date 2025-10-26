package com.example.schedulerjobs;

import com.example.schedulercore.Job;

public class DataBackupJob implements Job {
    @Override
    public void run(Job.JobContext ctx) throws Exception {
        Thread.sleep(500);
        System.out.println("[DataBackup] Executed #" + ctx.getExecutionId());
    }
}
