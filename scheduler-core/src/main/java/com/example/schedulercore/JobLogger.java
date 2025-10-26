package com.example.schedulercore;

import java.time.LocalDateTime;

public final class JobLogger {
    private JobLogger() {
    }

    public static synchronized void info(String msg) {
        System.out.println("[" + LocalDateTime.now() + "] " + msg);
    }

    public static synchronized void error(String msg, Throwable t) {
        System.err.println("[" + LocalDateTime.now() + "] ERROR " + msg);
        if (t != null) t.printStackTrace(System.err);
    }
}
