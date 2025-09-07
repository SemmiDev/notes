package blog.sammi.lab.notes.infrastructure.queue;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class QueueJob {
    private String id;
    private String jobType;
    private Map<String, Object> payload;
    private int retryCount;
    private int maxRetries;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private String errorMessage;
    
    public QueueJob() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
        this.maxRetries = 3;
    }
    
    public QueueJob(String jobType, Map<String, Object> payload) {
        this();
        this.jobType = jobType;
        this.payload = payload;
        this.scheduledAt = LocalDateTime.now();
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public void incrementRetry() {
        this.retryCount++;
    }
}
