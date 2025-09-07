package blog.sammi.lab.notes.domain.service;

import java.util.Optional;

public interface CacheService {
    <T> void put(String key, T value, int expirationSeconds);
    <T> Optional<T> get(String key, Class<T> type);
    void delete(String key);
    void deletePattern(String pattern);
    boolean exists(String key);
}
