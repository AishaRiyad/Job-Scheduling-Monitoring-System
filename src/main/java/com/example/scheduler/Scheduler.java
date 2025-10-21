package com.example.scheduler;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler implements AutoCloseable {

    private Timer timer = new Timer(true);
    private ExecutorService workers;

    private JobRegistration jobRegistration;
    private JobHistory jobHistory;

    private Map<String, JobRuntimeDetails> runtimeDetails = new ConcurrentHashMap<>();
    private Map<String, TimerTask> timerTasks = new ConcurrentHashMap<>();
    private List<JobListener> listeners = new CopyOnWriteArrayList<>();

    public Scheduler(int workerPoolSize) {
        this.jobRegistration = new JobRegistration();
        this.jobHistory = new JobHistory();
        this.workers = Executors.newFixedThreadPool(Math.max(1, workerPoolSize));
    }

    public void addListener(JobListener listener) {
        listeners.add(listener);
    }

    public JobRegistration getJobRegistration() {
        return jobRegistration;
    }

    public JobHistory getJobHistory() {
        return jobHistory;
    }

    public void scheduleAllRegistered() {
        for (JobScheduling js : jobRegistration.getJobsList()) {
            schedule(js);
        }
    }

    public void schedule(JobScheduling jobScheduling) {
        String jobName = jobScheduling.getJobName();
        runtimeDetails.putIfAbsent(jobName, new JobRuntimeDetails(jobName));

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                triggerOnce(jobScheduling);
            }
        };

        timer.scheduleAtFixedRate(task, 0, jobScheduling.getTimeInterval());
        timerTasks.put(jobName, task);
    }

    private void triggerOnce(JobScheduling js) {
        workers.submit(() -> runWithRetry(js));
    }

    private void runWithRetry(JobScheduling cfg) {
        final String name = cfg.getJobName();
        final Job job = cfg.getJob();
        final JobRuntimeDetails info = runtimeDetails.get(name);

        int attempt = 0;
        boolean success = false;
        Exception lastEx = null;


        info.isStarted();
        notifyStart(name, attempt + 1);

        long start = System.currentTimeMillis();

        while (attempt <= cfg.getMaximumRetries()) {
            try {
                job.run();
                success = true;
                break;
            } catch (Exception ex) {
                lastEx = ex;
                attempt++;
                if (attempt > cfg.getMaximumRetries()) break;
                try {

                    Thread.sleep(cfg.getRetryDelay());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    lastEx = ie;
                    break;
                }
            }
        }

        long end = System.currentTimeMillis();

        if (success) {
            info.isSuccess("OK");
            JobResult result = new JobResult(name, start, end, JobStatus.COMPLETED, "OK", null, attempt + 1);
            jobHistory.addResult(result);
            notifySuccess(name, result);
        } else {
            String err = (lastEx != null) ? (lastEx.getClass().getSimpleName() + ": " + lastEx.getMessage()) : "Unknown error";
            info.isFailure(err);
            JobResult result = new JobResult(name, start, end, JobStatus.FAILED, null, err, attempt);
            jobHistory.addResult(result);
            notifyFailure(name, result);
        }
    }

    private void notifyStart(String jobName, int attempt) {
        for (JobListener l : listeners) l.onStart(jobName, attempt);
    }

    private void notifySuccess(String jobName, JobResult r) {
        for (JobListener l : listeners) l.onSuccess(jobName, r);
    }

    private void notifyFailure(String jobName, JobResult r) {
        for (JobListener l : listeners) l.onFailure(jobName, r);
    }

    public java.util.List<String> runningJobs() {
        java.util.List<String> list = new java.util.ArrayList<>();
        for (JobRuntimeDetails d : runtimeDetails.values()) {
            if (d.getStatus() == JobStatus.RUNNING) {
                list.add(d.getJobName());
            }
        }
        return list;
    }


    public java.util.Map<String, JobRuntimeDetails> snapshotRuntime() {
        return new java.util.HashMap<>(runtimeDetails);
    }

    public void cancelJob(String jobName) {
        TimerTask task = timerTasks.remove(jobName);
        if (task != null) task.cancel();
    }

    @Override
    public void close() {

        for (TimerTask t : timerTasks.values()) t.cancel();
        timerTasks.clear();
        timer.cancel();
        workers.shutdownNow();
    }
}
