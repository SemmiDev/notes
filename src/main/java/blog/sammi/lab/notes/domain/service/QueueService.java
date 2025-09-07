package blog.sammi.lab.notes.domain.service;

import java.util.Map;

public interface QueueService {
    void enqueue(String queueName, String jobType, Map<String, Object> payload);
    void enqueue(String queueName, String jobType, Map<String, Object> payload, long delaySeconds);
    void scheduleJob(String jobType, Map<String, Object> payload, long delaySeconds);
}
