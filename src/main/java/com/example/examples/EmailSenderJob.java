package com.example.examples;

import com.example.scheduler.Job;

public class EmailSenderJob implements Job {
    @Override
    public void run() throws Exception{
        Thread.sleep(150);
    }
}
