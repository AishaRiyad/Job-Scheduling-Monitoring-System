package com.example.scheduler;

public interface JobListener {
    default void onStart(String jobName, int attempt) {
    }

    default void onSuccess(String jobName, JobResult result) {
    }

    default void onFailure(String jobName, JobResult result) {
    }
}
