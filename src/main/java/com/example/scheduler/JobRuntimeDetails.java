package com.example.scheduler;

public class JobRuntimeDetails {
    public String jobName;
    private JobStatus jobStatus = JobStatus.PENDING;
    private long lastStartTime;
    private long lastEndTime;
    private long lastDuration;
    private String lastOutcome;
    private String lastErrorMessage;

    public JobRuntimeDetails(String jobName) {
        this.jobName = jobName;
    }

    public synchronized JobStatus getStatus() {
        return jobStatus;
    }

    public synchronized void setStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public synchronized void isStarted() {
        lastStartTime = System.currentTimeMillis();
        jobStatus = JobStatus.RUNNING;

    }

    public synchronized void isSuccess(String outcome) {
        lastEndTime = System.currentTimeMillis();
        lastDuration = lastEndTime - lastStartTime;
        lastErrorMessage = null;
        lastOutcome = outcome;
        jobStatus = JobStatus.COMPLETED;
    }

    public synchronized void isFailure(String errorMessage) {
        lastEndTime = System.currentTimeMillis();
        lastDuration = lastEndTime - lastStartTime;
        lastErrorMessage = errorMessage;
        lastOutcome = null;
        jobStatus = JobStatus.FAILED;
    }

    public String getJobName() {
        return jobName;
    }

    public synchronized long getLastDuration() {
        return lastDuration;
    }

    public synchronized long getLastEndTime() {
        return lastEndTime;
    }

    public synchronized long getLastStartTime() {
        return lastStartTime;
    }

    public synchronized String getLastOutcome() {
        return lastOutcome;
    }

    public synchronized String getLastErrorMessage() {
        return lastErrorMessage;
    }


}
