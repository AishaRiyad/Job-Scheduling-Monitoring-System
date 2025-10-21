package com.example.scheduler;

public class ConsoleLoggingListener implements JobListener {
    @Override
    public void onStart(String jobName, int attempt) {
        System.out.println("[LISTENER] START " + jobName + " attempt=" + attempt);
    }

    @Override
    public void onSuccess(String jobName, JobResult result) {
        System.out.println("[LISTENER] SUCCESS " + jobName + " durationMs=" + result.getDuration()
                + " attempt=" + result.getAttempt());
    }

    @Override
    public void onFailure(String jobName, JobResult result) {
        System.out.println("[LISTENER] FAILURE " + jobName + " error=" + result.getErrorMessage()
                + " attempt=" + result.getAttempt());
    }
}
