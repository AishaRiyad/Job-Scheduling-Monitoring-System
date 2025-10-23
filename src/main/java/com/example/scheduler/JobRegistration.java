package com.example.scheduler;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobRegistration {
    private Map<String, JobScheduling> jobs = new ConcurrentHashMap<>();

    public void registerJob(JobScheduling jobScheduling) {
        String jobName = jobScheduling.getJobName();
        if (jobs.containsKey(jobName)) {
            throw new IllegalArgumentException("Job already exists");
        }
        jobs.put(jobName, jobScheduling);
    }

    public Collection<JobScheduling> getJobsList() {
        return jobs.values();
    }

    public JobScheduling getJob(String jobName) {
        JobScheduling job = jobs.get(jobName);
        if (job == null) {
            throw new IllegalArgumentException(jobName + " doesn't exist ");
        }
        return job;
    }
}
