System Overview
The "Concurrent Job Scheduler" is a modular, event-driven, and multithreaded system designed to manage recurring background jobs such as data backup, report generation, and log cleanup.  
It demonstrates key software engineering principles including "OOP design", "Java concurrency", and "design patterns" (Observer, Strategy, Factory, and Singleton).

The system includes:
- Job Scheduler Core — Manages job registration, worker threads, scheduling, and status tracking.
- Job Modules — Defines example jobs (`DataBackupJob`, `LogCleanupJob`, `ReportGeneratorJob`) that run at different intervals.
- CLI Interface — Provides a simple text-based menu to control and monitor the scheduler in real time.
- Notification & Logging System — Prints status updates, job outcomes, and system reports using console output.

How to Run the App:
Prerequisites:
- Java 8 or higher installed  
- Apache Maven installed  
- IntelliJ IDEA (recommended for running and testing)

  Run Using IntelliJ IDEA
1. Open the project folder "Task1" in IntelliJ IDEA.
2. Make sure Maven has imported all modules successfully.
3. Open the class: scheduler-jobs/src/main/java/com/example/schedulerjobs/CliMain.java
4. Right-click the file and select Run 'CliMain.main()'.
5. The command-line scheduler menu will appear

   Run Using Terminal
From the project root directory:
```bash
mvn -U clean install
cd scheduler-jobs
mvn exec:java -Dexec.mainClass="com.example.schedulerjobs.CliMain"


How to Run Tests

From the root directory:

mvn test

Or in IntelliJ:

Open the scheduler-tests module.

Right-click → Run All Tests.


The test suite includes JUnit 5 tests for:

-Scheduler initialization
-Job execution logic
-Concurrency and worker behavior
-Error handling and retries

Example Output:
When the scheduler is running:

===== JOB SCHEDULER MENU =====
1) Start scheduler
2) Stop scheduler
3) Show worker states
4) Show all jobs
5) Report for a worker (worker-1 / worker-2)
6) Success/Failed summary (last hour)
7) Mute status printer
8) Show recent history
9) Exit
Choose option [1-9]: 1

[Scheduler started with 2 workers]
[2025-10-26T00:26:55.567] DataBackupJob -> COMPLETED (501 ms)
[2025-10-26T00:26:57.464] ReportGeneratorJob -> COMPLETED (401 ms)
[2025-10-26T00:26:57.862] LogCleanupJob -> COMPLETED (800 ms)
[2025-10-26T00:26:59.091] Worker States: worker-1=IDLE, worker-2=IDLE


Viewing Reports:
Choose option [1-9]: 5
Enter worker id (worker-1 or worker-2): worker-1
Report for worker-1 (latest 5):
 - Job: DataBackupJob | Status: COMPLETED | Duration: 501 ms
 - Job: ReportGeneratorJob | Status: FAILED | Duration: 0 ms


Summary Example:
Choose option [1-9]: 6
Summary (last hour): {COMPLETED=45, FAILED=9}








   

