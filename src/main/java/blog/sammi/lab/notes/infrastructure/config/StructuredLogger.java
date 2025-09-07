package blog.sammi.lab.notes.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StructuredLogger {
    
    private final ObjectMapper objectMapper;
    
    public void logBusinessEvent(String event, String action, Object data) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("event", event);
            logData.put("action", action);
            logData.put("timestamp", System.currentTimeMillis());
            logData.put("data", data);
            
            String jsonLog = objectMapper.writeValueAsString(logData);
            log.info("Business Event: {}", jsonLog);
        } catch (Exception e) {
            log.error("Failed to log business event", e);
        }
    }
    
    public void logSecurityEvent(String event, String username, String details) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("event", "SECURITY");
            logData.put("action", event);
            logData.put("username", username);
            logData.put("details", details);
            logData.put("timestamp", System.currentTimeMillis());
            logData.put("correlationId", MDC.get("correlationId"));
            
            String jsonLog = objectMapper.writeValueAsString(logData);
            log.warn("Security Event: {}", jsonLog);
        } catch (Exception e) {
            log.error("Failed to log security event", e);
        }
    }
    
    public void logPerformanceMetric(String operation, long duration, Map<String, Object> metrics) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("event", "PERFORMANCE");
            logData.put("operation", operation);
            logData.put("duration", duration);
            logData.put("metrics", metrics);
            logData.put("timestamp", System.currentTimeMillis());
            
            String jsonLog = objectMapper.writeValueAsString(logData);
            log.info("Performance Metric: {}", jsonLog);
        } catch (Exception e) {
            log.error("Failed to log performance metric", e);
        }
    }
    
    public void logError(String operation, Exception error, Map<String, Object> context) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("event", "ERROR");
            logData.put("operation", operation);
            logData.put("error", error.getClass().getSimpleName());
            logData.put("message", error.getMessage());
            logData.put("context", context);
            logData.put("timestamp", System.currentTimeMillis());
            
            String jsonLog = objectMapper.writeValueAsString(logData);
            log.error("Application Error: {}", jsonLog, error);
        } catch (Exception e) {
            log.error("Failed to log error event", e);
        }
    }
}
