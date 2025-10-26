package com.example.schedulercore;

import java.time.LocalDateTime;

public interface AppEvent {
    EventType getType();
    LocalDateTime getTimestamp();
    String summary();
}
