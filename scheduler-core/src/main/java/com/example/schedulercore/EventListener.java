package com.example.schedulercore;

@FunctionalInterface
public interface EventListener {
    void onEvent(AppEvent e);
}
