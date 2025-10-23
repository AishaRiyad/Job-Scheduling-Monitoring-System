package com.example.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ListenerTest {

    private Scheduler scheduler;

    static class RecordingListener implements JobListener {
        final CopyOnWriteArrayList<String> events = new CopyOnWriteArrayList<>();

        @Override
        public void onStart(String jobName, int attempt) {
            events.add("start:" + jobName + ":" + attempt);
        }

        @Override
        public void onSuccess(String jobName, JobResult result) {
            events.add("success:" + jobName + ":" + result.getAttempt());
        }

        @Override
        public void onFailure(String jobName, JobResult result) {
            events.add("failure:" + jobName + ":" + result.getAttempt());
        }
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null) scheduler.close();
    }

    @Test
    void listener_receives_callbacks() {
        scheduler = new Scheduler(2);
        RecordingListener listener = new RecordingListener();
        scheduler.addListener(listener);

        Job ok = () -> {};
        JobScheduling cfg = new JobScheduling("EmailSenderJob", ok, 100L, 0, 0);
        scheduler.getJobRegistration().registerJob(cfg);
        scheduler.scheduleAllRegistered();

        boolean gotEvents = TestUtils.waitUntilTrue(() -> !listener.events.isEmpty(), 3000);
        assertTrue(gotEvents, "Listener didn't receive events in time");

        boolean hasStart = listener.events.stream().anyMatch(e -> e.startsWith("start:EmailSenderJob"));
        boolean hasSuccess = listener.events.stream().anyMatch(e -> e.startsWith("success:EmailSenderJob"));

        assertTrue(hasStart);
        assertTrue(hasSuccess);
    }
}
