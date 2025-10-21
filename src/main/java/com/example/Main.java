package com.example;

import com.example.scheduler.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] arg) throws Exception {
        try (Scheduler scheduler = new Scheduler(4)) {

            scheduler.addListener(new ConsoleLoggingListener());

            scheduler.addListener(new JobListener() {
                @Override
                public void onStart(String jobName, int attempt) {
                    System.out.println(" START " + jobName + " attempt=" + attempt);
                }

                @Override
                public void onSuccess(String jobName, JobResult r) {
                    System.out.println(" SUCCESS " + r);
                }

                @Override
                public void onFailure(String jobName, JobResult r) {
                    System.out.println(" FAILURE " + r);
                }
            });


            JobRegistration reg = scheduler.getJobRegistration();

            reg.registerJob(new JobScheduling("DataBackupJob", () -> {

                Thread.sleep(300);
                System.out.println("[DataBackupJob] Backup completed.");
            }, 30_000));

            reg.registerJob(new JobScheduling("LogCleanupJob", () -> {
                Thread.sleep(200);
                System.out.println("[LogCleanupJob] Old logs deleted.");
            }, 60_000));

            reg.registerJob(new JobScheduling("ReportGeneratorJob", () -> {
                Thread.sleep(400);

                if (Math.random() < 0.33) {
                    throw new RuntimeException("Report data source unavailable");
                }
                System.out.println("[ReportGeneratorJob] Report generated.");
            }, 20_000, 2, 5_000));

            reg.registerJob(new JobScheduling("EmailSenderJob", () -> {
                Thread.sleep(150);
                System.out.println("[EmailSenderJob] Summary email sent.");
            }, 60_000));


            scheduler.scheduleAllRegistered();


            Thread.sleep(120_000);


            System.out.println("-- Running jobs now: " + scheduler.runningJobs());
            for (var e : scheduler.snapshotRuntime().entrySet()) {
                JobRuntimeDetails info = e.getValue();
                System.out.printf("[STAT] %s status=%s lastDur=%dms lastErr=%s%n",
                        info.getJobName(), info.getStatus(),
                        info.getLastDuration(),
                        info.getLastErrorMessage());
            }


            Reporting reporting = new Reporting(scheduler.getJobHistory());
            System.out.println(" Count by status (last hour): " + reporting.countByStatusLastHour());
        }

    }
}