package blog.sammi.lab.notes.infrastructure.queue.processors;

import blog.sammi.lab.notes.domain.service.EmailService;
import blog.sammi.lab.notes.domain.service.JobProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailJobProcessor implements JobProcessor {
    
    private final EmailService emailService;
    
    @Override
    public void process(Map<String, Object> payload) throws Exception {
        String type = (String) payload.get("type");
        String to = (String) payload.get("to");
        String otpCode = (String) payload.get("otpCode");
        
        if (to == null || otpCode == null) {
            throw new IllegalArgumentException("Missing required email parameters");
        }
        
        switch (type) {
            case "OTP_VERIFICATION":
                emailService.sendOtpEmail(to, otpCode);
                break;
            case "PASSWORD_RESET":
                emailService.sendPasswordResetEmail(to, otpCode);
                break;
            default:
                throw new IllegalArgumentException("Unknown email type: " + type);
        }
        
        log.info("Email sent successfully: {} to {}", type, to);
    }
    
    @Override
    public String getJobType() {
        return "EMAIL";
    }
    
    @Override
    public int getMaxRetries() {
        return 3;
    }
}
