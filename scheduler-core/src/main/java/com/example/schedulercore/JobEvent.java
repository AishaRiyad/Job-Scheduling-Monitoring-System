package com.example.schedulercore;

import java.time.LocalDateTime;
import java.util.Objects;

public final class JobEvent implements AppEvent {
    private final EventType type;
    private final LocalDateTime timestamp;
    private final String jobName;
    private final String executionId;
    private final Status status;
    private final String message;

    private JobEvent(Builder b){
        this.type = Objects.requireNonNull(b.type);
        this.timestamp = b.timestamp != null ? b.timestamp : LocalDateTime.now();
        this.jobName = b.jobName;
        this.executionId = b.executionId;
        this.status = b.status;
        this.message = b.message;
    }

    public static class Builder {
        private EventType type;
        private LocalDateTime timestamp;
        private String jobName, executionId, message;
        private Status status;
        public Builder type(EventType v){ this.type=v; return this; }
        public Builder timestamp(LocalDateTime v){ this.timestamp=v; return this; }
        public Builder jobName(String v){ this.jobName=v; return this; }
        public Builder executionId(String v){ this.executionId=v; return this; }
        public Builder status(Status v){ this.status=v; return this; }
        public Builder message(String v){ this.message=v; return this; }
        public JobEvent build(){ return new JobEvent(this); }
    }

    @Override public EventType getType(){ return type; }
    @Override public LocalDateTime getTimestamp(){ return timestamp; }
    public String getJobName(){ return jobName; }
    public String getExecutionId(){ return executionId; }
    public Status getStatus(){ return status; }
    public String getMessage(){ return message; }

    @Override public String summary(){
        switch (type){
            case JOB_STARTED:
                return String.format("%s | STARTED  | %-18s | %s", timestamp, jobName, executionId);
            case JOB_COMPLETED:
                return String.format("%s | SUCCESS  | %-18s | %s | %s", timestamp, jobName, executionId, message == null ? "" : message);
            case JOB_FAILED:
                return String.format("%s | FAILED   | %-18s | %s | %s", timestamp, jobName, executionId, message == null ? "" : message);
            default:
                return String.format("%s | %-8s | %-18s | %s", timestamp, type, jobName, executionId);
        }
    }
}
