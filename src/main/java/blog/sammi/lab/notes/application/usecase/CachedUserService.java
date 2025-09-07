package blog.sammi.lab.notes.application.usecase;

import blog.sammi.lab.notes.application.dto.UserDto;
import blog.sammi.lab.notes.application.mapper.UserMapper;
import blog.sammi.lab.notes.domain.entity.User;
import blog.sammi.lab.notes.domain.repository.UserRepository;
import blog.sammi.lab.notes.domain.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedUserService {
    
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final UserMapper userMapper;
    
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String USER_EMAIL_CACHE_PREFIX = "user:email:";
    private static final String USER_USERNAME_CACHE_PREFIX = "user:username:";
    private static final int CACHE_TTL_SECONDS = 3600; // 1 hour
    
    // Read-through cache pattern
    public Optional<UserDto> findById(UUID id) {
        String cacheKey = USER_CACHE_PREFIX + id;
        
        // Try cache first
        Optional<UserDto> cached = cacheService.get(cacheKey, UserDto.class);
        if (cached.isPresent()) {
            log.debug("User found in cache: {}", id);
            return cached;
        }
        
        // Cache miss - fetch from database
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            UserDto userDto = userMapper.toDto(user.get());
            // Write to cache
            cacheService.put(cacheKey, userDto, CACHE_TTL_SECONDS);
            log.debug("User loaded from database and cached: {}", id);
            return Optional.of(userDto);
        }
        
        return Optional.empty();
    }
    
    // Read-through cache pattern for email lookup
    public Optional<UserDto> findByEmail(String email) {
        String cacheKey = USER_EMAIL_CACHE_PREFIX + email;
        
        Optional<UserDto> cached = cacheService.get(cacheKey, UserDto.class);
        if (cached.isPresent()) {
            return cached;
        }
        
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            UserDto userDto = userMapper.toDto(user.get());
            cacheService.put(cacheKey, userDto, CACHE_TTL_SECONDS);
            // Also cache by ID
            cacheService.put(USER_CACHE_PREFIX + userDto.id(), userDto, CACHE_TTL_SECONDS);
            return Optional.of(userDto);
        }
        
        return Optional.empty();
    }
    
    // Read-through cache pattern for username lookup
    public Optional<UserDto> findByUsername(String username) {
        String cacheKey = USER_USERNAME_CACHE_PREFIX + username;
        
        Optional<UserDto> cached = cacheService.get(cacheKey, UserDto.class);
        if (cached.isPresent()) {
            return cached;
        }
        
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            UserDto userDto = userMapper.toDto(user.get());
            cacheService.put(cacheKey, userDto, CACHE_TTL_SECONDS);
            // Also cache by ID and email
            cacheService.put(USER_CACHE_PREFIX + userDto.id(), userDto, CACHE_TTL_SECONDS);
            cacheService.put(USER_EMAIL_CACHE_PREFIX + userDto.email(), userDto, CACHE_TTL_SECONDS);
            return Optional.of(userDto);
        }
        
        return Optional.empty();
    }
    
    // Write-through cache pattern
    public UserDto save(User user) {
        User savedUser = userRepository.save(user);
        UserDto userDto = userMapper.toDto(savedUser);
        
        // Update all cache entries
        cacheService.put(USER_CACHE_PREFIX + userDto.id(), userDto, CACHE_TTL_SECONDS);
        cacheService.put(USER_EMAIL_CACHE_PREFIX + userDto.email(), userDto, CACHE_TTL_SECONDS);
        cacheService.put(USER_USERNAME_CACHE_PREFIX + userDto.username(), userDto, CACHE_TTL_SECONDS);
        
        log.debug("User saved and cached: {}", userDto.id());
        return userDto;
    }
    
    // Cache invalidation
    public void invalidateUserCache(UUID userId, String email, String username) {
        cacheService.delete(USER_CACHE_PREFIX + userId);
        cacheService.delete(USER_EMAIL_CACHE_PREFIX + email);
        cacheService.delete(USER_USERNAME_CACHE_PREFIX + username);
        
        log.debug("User cache invalidated: {}", userId);
    }
    
    // Cache warming - preload frequently accessed users
    public void warmCache(UUID userId) {
        findById(userId); // This will load and cache the user
    }
}
