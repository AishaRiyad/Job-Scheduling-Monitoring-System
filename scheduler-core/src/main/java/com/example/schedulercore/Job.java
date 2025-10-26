package com.example.schedulercore;

@FunctionalInterface
public interface Job {
    void run(JobContext ctx) throws Exception;

    final class JobContext {
        private final String executionId;

        public JobContext(String executionId) {
            this.executionId = executionId;
        }

        public String getExecutionId() {
            return executionId;
        }
    }
}
