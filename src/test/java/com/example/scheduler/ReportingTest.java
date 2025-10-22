package com.example.scheduler;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportingTest {

    @Test
    void reporting_grouping_sorting() {
        JobHistory history = new JobHistory();

        long t0 = System.currentTimeMillis();
        long t1 = t0 + 10;
        long t2 = t0 + 30;
        long t3 = t0 + 70;

        history.addResult(new JobResult("A", t0, t1, JobStatus.COMPLETED, "OK", null, 1));
        history.addResult(new JobResult("B", t0, t2, JobStatus.FAILED, null, "X", 2));
        history.addResult(new JobResult("C", t0, t3, JobStatus.FAILED, null, "Y", 1));

        Reporting reporting = new Reporting(history);

        Map<JobStatus, Long> counts = reporting.countByStatusLastHour();
        assertEquals(1L, counts.getOrDefault(JobStatus.COMPLETED, 0L));
        assertEquals(2L, counts.getOrDefault(JobStatus.FAILED, 0L));

        List<JobResult> failedSorted = reporting.failedSortedLastHourByDuration();
        assertEquals(2, failedSorted.size());
        assertTrue(failedSorted.get(0).getDuration() <= failedSorted.get(1).getDuration());

        List<JobResult> allDesc = reporting.sortAllByDurationDesc();
        assertEquals(3, allDesc.size());
        assertTrue(allDesc.get(0).getDuration() >= allDesc.get(1).getDuration());
        assertTrue(allDesc.get(1).getDuration() >= allDesc.get(2).getDuration());
    }
}
