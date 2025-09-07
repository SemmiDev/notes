package blog.sammi.lab.notes.domain.service;

import java.util.Map;

public interface JobProcessor {
    void process(Map<String, Object> payload) throws Exception;
    String getJobType();
    int getMaxRetries();
}
