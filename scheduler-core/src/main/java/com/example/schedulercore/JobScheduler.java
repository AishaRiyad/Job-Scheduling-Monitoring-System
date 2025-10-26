package com.example.schedulercore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.example.schedulercore.EventType.*;

public class JobScheduler implements AutoCloseable {

    private final Map<String, JobDef> registry = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<JobResult> history = new CopyOnWriteArrayList<>();
    private final ExecutorService workers = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private final AtomicInteger c = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "worker-" + c.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });
    private final Map<String, String> workerStates = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<JobResult>> workerHistory = new ConcurrentHashMap<>();
    private final Object consoleLock = new Object();
    private volatile boolean running = false;
    private volatile boolean muted = false;
    private Thread statusThread;

    public JobScheduler() {
        initWorker("worker-1");
        initWorker("worker-2");
    }

    public Object consoleLock() {
        return consoleLock;
    }

    private void initWorker(String name) {
        workerStates.put(name, "IDLE");
        workerHistory.put(name, new CopyOnWriteArrayList<>());
    }

    public void muteStatus() {
        muted = true;
    }

    public void unmuteStatus() {
        muted = false;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isRunning() {
        return running;
    }

    public void register(String name, Duration interval, Job job, RetryPolicy retry) {
        registry.put(name, new JobDef(name, interval, job, retry));
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        registry.values().forEach(def -> {
            Thread loop = new Thread(() -> runLoop(def), "loop-" + def.name);
            loop.setDaemon(true);
            loop.start();
        });
        if (statusThread == null || !statusThread.isAlive()) {
            statusThread = new Thread(this::statusLoop, "status-printer");
            statusThread.setDaemon(true);
            statusThread.start();
        }
        JobLogger.info("Scheduler started.");
    }

    public synchronized void stop() {
        running = false;
        workers.shutdownNow();
        if (statusThread != null) statusThread.interrupt();
        JobLogger.info("Scheduler stopped.");
    }

    private void runLoop(JobDef def) {
        while (running) {
            submit(def);
            try {
                Thread.sleep(def.interval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void submit(JobDef def) {
        workers.submit(() -> execute(def));
    }

    private void execute(JobDef def) {
        String wn = Thread.currentThread().getName();
        updateWorker(wn, "RUNNING " + def.name);

        LocalDateTime start = LocalDateTime.now();
        String execId = UUID.randomUUID().toString();

        EventDispatcher.getInstance().publish(
                new JobEvent.Builder()
                        .type(JOB_STARTED)
                        .timestamp(start)
                        .jobName(def.name)
                        .executionId(execId)
                        .build()
        );

        int attempt = 0;
        boolean ok = false;
        Throwable last = null;

        do {
            try {
                def.job.run(new Job.JobContext(execId));
                ok = true;
            } catch (Exception e) {
                last = e;
                if (def.retry == null || !def.retry.shouldRetry(++attempt, e)) break;
                try {
                    Thread.sleep(def.retry.nextDelay(attempt).toMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } while (!ok);

        LocalDateTime end = LocalDateTime.now();
        JobResult res = new JobResult.Builder()
                .jobName(def.name).executionId(execId)
                .start(start).end(end)
                .status(ok ? Status.COMPLETED : Status.FAILED)
                .message(ok ? "OK" : (last != null ? last.getMessage() : "Unknown"))
                .build();

        history.add(res);
        workerHistory.get(wn).add(res);

        EventDispatcher.getInstance().publish(
                new JobEvent.Builder()
                        .type(ok ? JOB_COMPLETED : JOB_FAILED)
                        .timestamp(end)
                        .jobName(def.name)
                        .executionId(execId)
                        .status(res.getStatus())
                        .message(res.getMessage())
                        .build()
        );

        JobLogger.info(def.name + " -> " + res.getStatus() + " (" + res.getDuration().toMillis() + " ms)");
        updateWorker(wn, "IDLE");
    }

    private void updateWorker(String worker, String state) {
        workerStates.put(worker, state);
    }

    private void statusLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
                if (!muted) {
                    synchronized (consoleLock) {
                        JobLogger.info("Worker States: " + workerStates.entrySet().stream()
                                .map(e -> e.getKey() + "=" + e.getValue())
                                .collect(Collectors.joining(", ")));
                        System.out.print("scheduler> ");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<JobResult> getHistory() {
        return history;
    }

    public Map<String, String> snapshotWorkerStates() {
        return new HashMap<>(workerStates);
    }

    public List<String> getWorkers() {
        return new ArrayList<>(workerStates.keySet());
    }

    public List<JobResult> getWorkerHistory(String worker) {
        CopyOnWriteArrayList<JobResult> list = workerHistory.get(worker);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    public Map<Status, Long> countByStatusLastHour() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        return history.stream()
                .filter(h -> h.getEndTime().isAfter(cutoff))
                .collect(Collectors.groupingBy(JobResult::getStatus, Collectors.counting()));
    }

    @Override
    public void close() {
        stop();
    }

    private static final class JobDef {
        final String name;
        final Duration interval;
        final Job job;
        final RetryPolicy retry;

        JobDef(String name, Duration interval, Job job, RetryPolicy retry) {
            this.name = name;
            this.interval = interval;
            this.job = job;
            this.retry = retry;
        }
    }
}
