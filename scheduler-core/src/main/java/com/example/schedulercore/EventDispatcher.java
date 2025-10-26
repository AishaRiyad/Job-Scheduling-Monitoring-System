package com.example.schedulercore;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class EventDispatcher {
    private static final EventDispatcher INSTANCE = new EventDispatcher();
    public static EventDispatcher getInstance(){ return INSTANCE; }

    private final Map<EventType, CopyOnWriteArrayList<EventListener>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService deliveryPool;
    private final Deque<AppEvent> recent = new LinkedList<>();
    private final int capacity = 200;

    private EventDispatcher(){
        for (EventType t : EventType.values()){
            listeners.put(t, new CopyOnWriteArrayList<>());
        }
        deliveryPool = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private final AtomicInteger c = new AtomicInteger(1);
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "event-dispatcher-" + c.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void subscribe(EventType type, EventListener l){
        listeners.get(type).add(Objects.requireNonNull(l));
    }

    public void unsubscribe(EventType type, EventListener l){
        listeners.get(type).remove(l);
    }

    public void publish(AppEvent e){
        synchronized (recent){
            recent.addLast(e);
            while (recent.size() > capacity) recent.removeFirst();
        }
        for (EventListener l : listeners.get(e.getType())){
            deliveryPool.submit(() -> {
                try { l.onEvent(e); } catch (Throwable ignore) {}
            });
        }
    }

    public List<AppEvent> recentEvents(){
        synchronized (recent){
            return new ArrayList<>(recent);
        }
    }
}
