package blog.sammi.lab.notes.infrastructure.email;

import blog.sammi.lab.notes.domain.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.email.mock", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MockEmailServiceImpl implements EmailService {
    
    @Override
    public void sendOtpEmail(String to, String otpCode) {
        log.info("🔥 MOCK EMAIL - OTP Verification");
        log.info("📧 To: {}", to);
        log.info("🔑 OTP Code: {}", otpCode);
        log.info("📝 Subject: Verify Your Account - Notes App");
        log.info("✅ Email would be sent successfully (MOCK MODE)");
    }
    
    @Override
    public void sendPasswordResetEmail(String to, String resetCode) {
        log.info("🔥 MOCK EMAIL - Password Reset");
        log.info("📧 To: {}", to);
        log.info("🔑 Reset Code: {}", resetCode);
        log.info("📝 Subject: Reset Your Password - Notes App");
        log.info("✅ Email would be sent successfully (MOCK MODE)");
    }
}
