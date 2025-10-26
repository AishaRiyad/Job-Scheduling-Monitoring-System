package com.example.schedulercore;

public class ConsoleNotification implements NotificationStrategy {
    @Override public void send(AppEvent e) {
        JobLogger.info("[NOTIFY] " + e.summary());
    }
}
