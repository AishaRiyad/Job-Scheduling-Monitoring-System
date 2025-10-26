package com.example.schedulercore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class JobResult {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String jobName;
    private final String executionId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Duration duration;
    private final Status status;
    private final String message;

    private JobResult(Builder b) {
        this.jobName = b.jobName;
        this.executionId = b.executionId;
        this.startTime = b.startTime;
        this.endTime = b.endTime;
        this.duration = Duration.between(startTime, endTime);
        this.status = b.status;
        this.message = b.message;
    }

    public String getJobName() {
        return jobName;
    }

    public String getExecutionId() {
        return executionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        String s = startTime != null ? startTime.format(FMT) : "-";
        String e = endTime != null ? endTime.format(FMT) : "-";
        long ms = duration != null ? duration.toMillis() : -1;
        return String.format("%s | %s | %6d ms | %-9s | %-18s | %s | %s",
                s, e, ms, status, jobName, executionId, (message == null ? "" : message));
    }

    public static class Builder {
        private String jobName, executionId, message;
        private LocalDateTime startTime, endTime;
        private Status status;

        public Builder jobName(String v) {
            this.jobName = v;
            return this;
        }

        public Builder executionId(String v) {
            this.executionId = v;
            return this;
        }

        public Builder start(LocalDateTime v) {
            this.startTime = v;
            return this;
        }

        public Builder end(LocalDateTime v) {
            this.endTime = v;
            return this;
        }

        public Builder status(Status v) {
            this.status = v;
            return this;
        }

        public Builder message(String v) {
            this.message = v;
            return this;
        }

        public JobResult build() {
            return new JobResult(this);
        }
    }
}
