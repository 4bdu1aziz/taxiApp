package com.taxiapp.notification.service;

import com.taxiapp.notification.entity.NotificationTask;
import com.taxiapp.notification.repository.NotificationTaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class NotificationWorkerService {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerService.class);
    private final NotificationTaskRepository repository;
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private static final int POOL_SIZE = 4;
    private static final int MAX_RETRIES = 3;

    public NotificationWorkerService(NotificationTaskRepository repository,
                                     PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    @PostConstruct
    public void startWorkers() {
        log.info("Starting {} notification workers...", POOL_SIZE);
        for (int i = 0; i < POOL_SIZE; i++) {
            final int workerId = i + 1;
            executor.submit(() -> {
                log.info("Worker {} started", workerId);
                while (running.get()) {
                    try {
                        processTask(workerId);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Worker {} error: {}", workerId, e.getMessage());
                    }
                }
                log.info("Worker {} stopped", workerId);
            });
        }
    }

    void processTask(int workerId) {
        transactionTemplate.execute(status -> {
            repository.findNextPendingTask().ifPresent(task -> {
                task.setStatus(NotificationTask.NotificationStatus.PROCESSING);
                task.setAttempts(task.getAttempts() + 1);
                repository.save(task);

                log.info("Worker {} processing task #{}: {}", workerId, task.getId(), task.getMessage());

                boolean sent = sendNotification(task);

                if (sent) {
                    task.setStatus(NotificationTask.NotificationStatus.SENT);
                    log.info("Worker {} SENT task #{}", workerId, task.getId());
                } else if (task.getAttempts() >= MAX_RETRIES) {
                    task.setStatus(NotificationTask.NotificationStatus.FAILED);
                    log.warn("Worker {} FAILED task #{}", workerId, task.getId());
                } else {
                    task.setStatus(NotificationTask.NotificationStatus.PENDING);
                    log.info("Worker {} retry task #{} (attempt {}/{})",
                            workerId, task.getId(), task.getAttempts(), MAX_RETRIES);
                }
                repository.save(task);
            });
            return null;
        });
    }

    private boolean sendNotification(NotificationTask task) {
        try {
            Thread.sleep(500 + (long)(Math.random() * 1000));
            boolean success = Math.random() < 0.9;
            String recipient = "DRIVER".equals(task.getRecipientType()) ? "Driver" : "Passenger";
            System.out.printf("[NOTIFICATION] To %s #%d: %s%n", recipient, task.getRecipientId(), task.getMessage());
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down workers...");
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("All workers stopped");
    }
}