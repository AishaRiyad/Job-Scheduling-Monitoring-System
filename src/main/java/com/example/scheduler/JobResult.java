package com.example.scheduler;

import java.util.Objects;

public class JobResult {
    private String jobName;
    private long startTime;
    private long endTime;
    private long duration;
    private JobStatus jobStatus;
    private String outcome;
    private String errorMessage;
    private int attempt;

    public JobResult(String jobName, long startTime, long endTime, JobStatus jobStatus, String outcome, String errorMessage, int attempt) {
        this.jobName = Objects.requireNonNull(jobName);
        if (endTime < startTime) {
            throw new IllegalArgumentException("End time cannot be less than start time");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
        this.jobStatus = Objects.requireNonNull(jobStatus);
        this.outcome = outcome;
        this.errorMessage = errorMessage;
        this.attempt = attempt;

    }

    public String getJobName() {
        return jobName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return duration;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getAttempt() {
        return attempt;
    }

    @Override
    public String toString() {
        return "JobResult{" +
                "jobName='" + jobName + '\'' +
                ", start=" + startTime +
                ", end=" + endTime +
                ", durationMs=" + duration +
                ", status=" + jobStatus +
                ", attempt=" + attempt +
                (outcome != null ? ", outcome='" + outcome + '\'' : "") +
                (errorMessage != null ? ", error='" + errorMessage + '\'' : "") +
                '}';
    }

}
