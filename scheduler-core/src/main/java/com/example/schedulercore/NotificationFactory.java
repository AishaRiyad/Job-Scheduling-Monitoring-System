package com.example.schedulercore;

public final class NotificationFactory {
    private NotificationFactory() {
    }

    public static NotificationStrategy of(String channel) {
        return new ConsoleNotification();
    }
}
