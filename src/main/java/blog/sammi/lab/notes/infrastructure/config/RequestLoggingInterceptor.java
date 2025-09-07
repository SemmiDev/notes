package blog.sammi.lab.notes.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    private static final String START_TIME_ATTRIBUTE = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        
        log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            if (ex != null) {
                log.error("Request completed with error: {} {} - Status: {} - Duration: {}ms - Error: {}", 
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, ex.getMessage());
            } else {
                log.info("Request completed: {} {} - Status: {} - Duration: {}ms", 
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            }
        }
    }
}
