package com.example.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobHistory {
    private List<JobResult> results = Collections.synchronizedList(new ArrayList<>());

    public void addResult(JobResult jobResult) {
        results.add(jobResult);
    }

    public List<JobResult> getResults() {
        synchronized (results) {
            return new ArrayList<>(results);
        }
    }

    public Stream<JobResult> getResultStream() {
        synchronized (results) {
            return getResults().stream();
        }
    }

    public List<JobResult> lastSince(long sinceMillis) {
        synchronized (results) {
            return results.stream()
                    .filter(r -> r.getEndTime() >= sinceMillis)
                    .collect(Collectors.toList());
        }
    }

    public List<JobResult> byJobName(String jobName) {
        synchronized (results) {
            return results.stream()
                    .filter(r -> r.getJobName().equals(jobName))
                    .collect(Collectors.toList());
        }
    }

    public Map<JobStatus, Long> countByStatusSince(long sinceMillis) {
        return lastSince(sinceMillis).stream()
                .collect(Collectors.groupingBy(JobResult::getJobStatus, Collectors.counting()));
    }

    public List<JobResult> failedOnlySince(long sinceMillis) {
        return lastSince(sinceMillis).stream()
                .filter(r -> r.getJobStatus() == JobStatus.FAILED)
                .collect(Collectors.toList());
    }
}
