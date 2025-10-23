package com.example.scheduler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Reporting {
    private JobHistory jobHistory;

    public Reporting(JobHistory jobHistory) {
        this.jobHistory = jobHistory;
    }

    public List<JobResult> lastHour() {
        long hourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        return jobHistory.lastSince(hourAgo);
    }

    public Map<JobStatus, Long> countByStatusLastHour() {
        long hourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        return jobHistory.countByStatusSince(hourAgo);
    }

    public List<JobResult> failedSortedLastHourByDuration() {
        return lastHour().stream()
                .filter(r -> r.getJobStatus() == JobStatus.FAILED)
                .sorted(Comparator.comparing(JobResult::getDuration))
                .collect(Collectors.toList());
    }

    public List<JobResult> sortAllByDurationDesc() {
        return jobHistory.getResults().stream()
                .sorted(Comparator.comparing(JobResult::getDuration).reversed())
                .collect(Collectors.toList());
    }


}
