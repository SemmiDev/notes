package blog.sammi.lab.notes.infrastructure.queue;

import blog.sammi.lab.notes.domain.service.JobProcessor;
import blog.sammi.lab.notes.infrastructure.config.StructuredLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueWorker {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final List<JobProcessor> jobProcessors;
    private final StructuredLogger structuredLogger;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;

    private static final String QUEUE_PREFIX = "queue:";
    private static final String RETRY_QUEUE_PREFIX = "retry:";
    private static final String DEAD_LETTER_QUEUE = "dead_letter";

    @EventListener(ApplicationReadyEvent.class)
    public void startWorkers() {
        if (running.compareAndSet(false, true)) {
            executorService = Executors.newFixedThreadPool(3);

            // Start workers for different queues
            executorService.submit(() -> processQueue("default"));
            executorService.submit(() -> processQueue("email"));
            executorService.submit(() -> processQueue("notifications"));

            log.info("Queue workers started");
        }
    }

    private void processQueue(String queueName) {
        RQueue<String> queue = redissonClient.getQueue(QUEUE_PREFIX + queueName);

        while (running.get()) {
            try {
                String jobJson = queue.poll();
                if (jobJson != null) {
                    processJob(jobJson, queueName);
                } else {
                    // Sleep briefly if no jobs available
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing queue: {}", queueName, e);
            }
        }
    }

    private void processJob(String jobJson, String queueName) {
        try {
            QueueJob job = objectMapper.readValue(jobJson, QueueJob.class);
            JobProcessor processor = findProcessor(job.getJobType());

            if (processor == null) {
                log.error("No processor found for job type: {}", job.getJobType());
                moveToDeadLetter(job, "No processor found");
                return;
            }

            long startTime = System.currentTimeMillis();

            try {
                processor.process(job.getPayload());

                long duration = System.currentTimeMillis() - startTime;
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("jobId", job.getId());
                eventData.put("jobType", job.getJobType());
                eventData.put("queueName", queueName);
                eventData.put("duration", duration);
                structuredLogger.logBusinessEvent("JOB_PROCESSED", "SUCCESS", eventData);

                log.info("Job processed successfully: {} in {}ms", job.getId(), duration);

            } catch (Exception e) {
                handleJobFailure(job, queueName, e);
            }

        } catch (Exception e) {
            log.error("Failed to parse job JSON: {}", jobJson, e);
        }
    }

    private void handleJobFailure(QueueJob job, String queueName, Exception e) {
        job.incrementRetry();
        job.setErrorMessage(e.getMessage());

        Map<String, Object> context = new HashMap<>();
        context.put("jobId", job.getId());
        context.put("jobType", job.getJobType());
        context.put("retryCount", job.getRetryCount());
        context.put("maxRetries", job.getMaxRetries());
        structuredLogger.logError("JOB_PROCESSING_FAILED", e, context);

        if (job.canRetry()) {
            // Exponential backoff: 2^retryCount seconds
            long delaySeconds = (long) Math.pow(2, job.getRetryCount());
            scheduleRetry(job, queueName, delaySeconds);

            log.warn("Job failed, scheduling retry {}/{} in {}s: {}",
                job.getRetryCount(), job.getMaxRetries(), delaySeconds, job.getId());
        } else {
            moveToDeadLetter(job, e.getMessage());
            log.error("Job exhausted retries, moved to dead letter: {}", job.getId());
        }
    }

    private void scheduleRetry(QueueJob job, String queueName, long delaySeconds) {
        try {
            RQueue<String> retryQueue = redissonClient.getQueue(RETRY_QUEUE_PREFIX + queueName);
            String jobJson = objectMapper.writeValueAsString(job);

            // Schedule retry with delay
            redissonClient.getDelayedQueue(retryQueue).offer(jobJson, delaySeconds, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Failed to schedule retry for job: {}", job.getId(), e);
            moveToDeadLetter(job, "Failed to schedule retry: " + e.getMessage());
        }
    }

    private void moveToDeadLetter(QueueJob job, String reason) {
        try {
            job.setErrorMessage(reason);
            RQueue<String> deadLetterQueue = redissonClient.getQueue(DEAD_LETTER_QUEUE);
            String jobJson = objectMapper.writeValueAsString(job);
            deadLetterQueue.offer(jobJson);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("jobId", job.getId());
            eventData.put("jobType", job.getJobType());
            eventData.put("reason", reason);
            structuredLogger.logBusinessEvent("JOB_DEAD_LETTER", "FAILED", eventData);

        } catch (Exception e) {
            log.error("Failed to move job to dead letter queue: {}", job.getId(), e);
        }
    }

    private JobProcessor findProcessor(String jobType) {
        return jobProcessors.stream()
                .filter(processor -> processor.getJobType().equals(jobType))
                .findFirst()
                .orElse(null);
    }

    @PreDestroy
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            log.info("Shutting down queue workers...");

            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            log.info("Queue workers shut down completed");
        }
    }
}
