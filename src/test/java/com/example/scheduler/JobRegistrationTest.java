package com.example.scheduler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JobRegistrationTest {

    @Test
    void registerAndFetchJob_ok() {
        JobRegistration reg = new JobRegistration();
        Job simple = () -> {};
        JobScheduling cfg = new JobScheduling("FirstJob", simple, 100L, 0, 0);
        reg.registerJob(cfg);

        assertEquals(cfg, reg.getJob("FirstJob"));
        assertTrue(reg.getJobsList().contains(cfg));
    }

    @Test
    void duplicateName_throws() {
        JobRegistration reg = new JobRegistration();
        Job simple = () -> {};
        reg.registerJob(new JobScheduling("dupplicateJob", simple, 100L, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> reg.registerJob(new JobScheduling("dupplicateJob", simple, 200L, 0, 0)));
    }

    @Test
    void getMissing_throws() {
        JobRegistration reg = new JobRegistration();
        assertThrows(IllegalArgumentException.class, () -> reg.getJob("nope"));
    }
}
