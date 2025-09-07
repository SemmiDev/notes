package blog.sammi.lab.notes.infrastructure.queue;

import blog.sammi.lab.notes.domain.service.QueueService;
import blog.sammi.lab.notes.infrastructure.config.StructuredLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValkeyQueueService implements QueueService {
    
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final StructuredLogger structuredLogger;
    
    private static final String QUEUE_PREFIX = "queue:";
    private static final String DELAYED_QUEUE_PREFIX = "delayed:";
    
    @Override
    public void enqueue(String queueName, String jobType, Map<String, Object> payload) {
        try {
            QueueJob job = new QueueJob(jobType, payload);
            RQueue<String> queue = redissonClient.getQueue(QUEUE_PREFIX + queueName);
            
            String jobJson = objectMapper.writeValueAsString(job);
            queue.offer(jobJson);
            
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("queueName", queueName);
            eventData.put("jobType", jobType);
            eventData.put("jobId", job.getId());
            structuredLogger.logBusinessEvent("JOB_ENQUEUED", "SUCCESS", eventData);
            
            log.info("Job enqueued: {} to queue: {}", job.getId(), queueName);
            
        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("queueName", queueName);
            context.put("jobType", jobType);
            structuredLogger.logError("JOB_ENQUEUE", e, context);
            throw new RuntimeException("Failed to enqueue job", e);
        }
    }
    
    @Override
    public void enqueue(String queueName, String jobType, Map<String, Object> payload, long delaySeconds) {
        try {
            QueueJob job = new QueueJob(jobType, payload);
            job.setScheduledAt(job.getScheduledAt().plusSeconds(delaySeconds));
            
            RQueue<String> queue = redissonClient.getQueue(QUEUE_PREFIX + queueName);
            RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(queue);
            
            String jobJson = objectMapper.writeValueAsString(job);
            delayedQueue.offer(jobJson, delaySeconds, TimeUnit.SECONDS);
            
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("queueName", queueName);
            eventData.put("jobType", jobType);
            eventData.put("jobId", job.getId());
            eventData.put("delaySeconds", delaySeconds);
            structuredLogger.logBusinessEvent("JOB_SCHEDULED", "SUCCESS", eventData);
            
            log.info("Job scheduled: {} to queue: {} with delay: {}s", job.getId(), queueName, delaySeconds);
            
        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("queueName", queueName);
            context.put("jobType", jobType);
            context.put("delaySeconds", delaySeconds);
            structuredLogger.logError("JOB_SCHEDULE", e, context);
            throw new RuntimeException("Failed to schedule job", e);
        }
    }
    
    @Override
    public void scheduleJob(String jobType, Map<String, Object> payload, long delaySeconds) {
        enqueue("default", jobType, payload, delaySeconds);
    }
}
