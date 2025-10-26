package com.example.schedulerjobs;

import com.example.schedulercore.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CliMain {

    private static void printMenu(boolean running, boolean muted) {
        System.out.println();
        System.out.println("===== JOB SCHEDULER MENU =====");
        System.out.println(" 1) Start scheduler      " + (running ? "(running)" : ""));
        System.out.println(" 2) Stop scheduler       " + (!running ? "(stopped)" : ""));
        System.out.println(" 3) Show worker states");
        System.out.println(" 4) Show all jobs");
        System.out.println(" 5) Report for a worker (worker-1 / worker-2)");
        System.out.println(" 6) Success/Failed summary (last hour)");
        System.out.println(" 7) " + (muted ? "Unmute" : "Mute") + " status printer");
        System.out.println(" 8) Show recent history");
        System.out.println(" 9) Exit");
        System.out.println("10) Subscribe console notifications (events)");
        System.out.println("11) Show recent events");
        System.out.print("Choose option [1-11]: ");
    }

    public static void main(String[] args) {
        JobScheduler scheduler = new JobScheduler();

        scheduler.register("DataBackupJob", Duration.ofSeconds(3), new DataBackupJob(), null);
        scheduler.register("LogCleanupJob", Duration.ofSeconds(5), new LogCleanupJob(), null);
        scheduler.register("ReportGeneratorJob", Duration.ofSeconds(4), new ReportGeneratorJob(),
                RetryPolicy.fixedAttempts(1, Duration.ofSeconds(2)));

        Scanner sc = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            synchronized (scheduler.consoleLock()) {
                printMenu(scheduler.isRunning(), scheduler.isMuted());
            }
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    scheduler.start();
                    synchronized (scheduler.consoleLock()) {
                        System.out.println(">> Scheduler started.");
                    }
                    break;

                case "2":
                    scheduler.stop();
                    synchronized (scheduler.consoleLock()) {
                        System.out.println(">> Scheduler stopped.");
                    }
                    break;

                case "3":
                    Map<String, String> st = scheduler.snapshotWorkerStates();
                    synchronized (scheduler.consoleLock()) {
                        System.out.println("Worker States: " + st);
                    }
                    break;

                case "4":
                    synchronized (scheduler.consoleLock()) {
                        System.out.println("- DataBackupJob every 3s");
                        System.out.println("- LogCleanupJob every 5s");
                        System.out.println("- ReportGeneratorJob every 4s (retry x1 after 2s)");
                    }
                    break;

                case "5": {
                    synchronized (scheduler.consoleLock()) {
                        System.out.print("Enter worker id (worker-1 or worker-2): ");
                    }
                    String w = sc.nextLine().trim();
                    List<JobResult> list = scheduler.getWorkerHistory(w);
                    synchronized (scheduler.consoleLock()) {
                        if (list.isEmpty()) {
                            System.out.println("No executions recorded for '" + w + "'.");
                        } else {
                            System.out.println("Report for " + w + " (latest 20, newest first):");
                            System.out.println("Total Executions: " + list.size());
                            System.out.println("Start Time          | End Time            | Dur(ms) | Status    | Job                | ExecutionId                           | Message");
                            System.out.println("--------------------+---------------------+---------+-----------+--------------------+----------------------------------------+----------------");
                            list.stream()
                                    .sorted(Comparator.comparing(JobResult::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                                    .limit(20)
                                    .forEach(r -> System.out.println(r.toString()));
                        }
                    }
                }
                break;

                case "6":
                    Map<Status, Long> m = scheduler.countByStatusLastHour();
                    synchronized (scheduler.consoleLock()) {
                        System.out.println("Summary (last hour): " + m);
                    }
                    break;

                case "7":
                    if (scheduler.isMuted()) {
                        scheduler.unmuteStatus();
                        synchronized (scheduler.consoleLock()) {
                            System.out.println(">> Status printer: UNMUTED");
                        }
                    } else {
                        scheduler.muteStatus();
                        synchronized (scheduler.consoleLock()) {
                            System.out.println(">> Status printer: MUTED");
                        }
                    }
                    break;

                case "8": {
                    List<JobResult> all = scheduler.getHistory();
                    synchronized (scheduler.consoleLock()) {
                        if (all.isEmpty()) {
                            System.out.println("No history yet.");
                        } else {
                            System.out.println("Recent history (latest 30, newest first):");
                            System.out.println("Total History Records: " + all.size());
                            System.out.println("Start Time          | End Time            | Dur(ms) | Status    | Job                | ExecutionId                           | Message");
                            System.out.println("--------------------+---------------------+---------+-----------+--------------------+----------------------------------------+----------------");
                            all.stream()
                                    .sorted(Comparator.comparing(JobResult::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                                    .limit(30)
                                    .forEach(r -> System.out.println(r.toString()));
                        }
                    }
                }
                break;

                case "9":
                    scheduler.stop();
                    exit = true;
                    synchronized (scheduler.consoleLock()) {
                        System.out.println("Bye.");
                    }
                    break;

                case "10": {
                    EventListener l = NotificationFactory.of("console")::send;
                    EventDispatcher.getInstance().subscribe(EventType.JOB_STARTED, l);
                    EventDispatcher.getInstance().subscribe(EventType.JOB_COMPLETED, l);
                    EventDispatcher.getInstance().subscribe(EventType.JOB_FAILED, l);
                    synchronized (scheduler.consoleLock()) {
                        System.out.println(">> Subscribed console notifications for JOB_* events.");
                    }
                }
                break;

                case "11": {
                    List<AppEvent> list = EventDispatcher.getInstance().recentEvents();
                    synchronized (scheduler.consoleLock()) {
                        if (list.isEmpty()) {
                            System.out.println("No events yet.");
                        } else {
                            System.out.println("Recent events (newest first):");
                            list.stream()
                                    .sorted((a, b) -> {
                                        if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
                                        if (a.getTimestamp() == null) return 1;
                                        if (b.getTimestamp() == null) return -1;
                                        return b.getTimestamp().compareTo(a.getTimestamp());
                                    })
                                    .limit(50)
                                    .forEach(e -> System.out.println("  " + e.summary()));
                        }
                    }
                }
                break;

                default:
                    synchronized (scheduler.consoleLock()) {
                        System.out.println("Unknown option. Choose 1-11.");
                    }
            }
        }
    }
}
