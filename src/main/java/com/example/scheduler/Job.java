package com.example.scheduler;

@FunctionalInterface
public interface Job {
    void run() throws Exception;

    default String getName() {
        return getClass().getSimpleName();
    }

}
