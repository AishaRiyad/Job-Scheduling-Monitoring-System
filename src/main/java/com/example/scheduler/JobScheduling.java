package com.example.scheduler;

import java.util.Objects;

public class JobScheduling {
    private String jobName;
    private Job job;
    private long timeInterval;
    private int maximumRetries;
    private long retryDelay;

    public JobScheduling(String jobName, Job job, long timeInterval) {
        this(jobName, job, timeInterval, 0, 0);
    }

    public JobScheduling(String jobName, Job job, long timeInterval, int maximumRetries, long retryDelay) {
        this.jobName = Objects.requireNonNull(jobName);
        this.job = Objects.requireNonNull(job);
        if (timeInterval <= 0) {
            throw new IllegalArgumentException("TimeInterval must be greater than 0");
        }
        this.timeInterval = timeInterval;
        this.maximumRetries = Math.max(0, maximumRetries);
        this.retryDelay = Math.max(0, retryDelay);
    }

    public String getJobName() {
        return jobName;
    }

    public Job getJob() {
        return job;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public int getMaximumRetries() {
        return maximumRetries;
    }

    public void setMaximumRetries(int maximumRetries) {
        this.maximumRetries = maximumRetries;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
}
