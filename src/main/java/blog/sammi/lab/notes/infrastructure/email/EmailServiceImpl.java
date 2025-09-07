package blog.sammi.lab.notes.infrastructure.email;

import blog.sammi.lab.notes.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@ConditionalOnProperty(name = "app.email.mock", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Override
    public void sendOtpEmail(String to, String otpCode) {
        try {
            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            
            String htmlContent = templateEngine.process("otp-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "Notes App");
            helper.setTo(to);
            helper.setSubject("Verify Your Account - Notes App");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    
    @Override
    public void sendPasswordResetEmail(String to, String resetCode) {
        try {
            Context context = new Context();
            context.setVariable("resetCode", resetCode);
            
            String htmlContent = templateEngine.process("password-reset-email", context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, "Notes App");
            helper.setTo(to);
            helper.setSubject("Reset Your Password - Notes App");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
