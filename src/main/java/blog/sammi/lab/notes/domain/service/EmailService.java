package blog.sammi.lab.notes.domain.service;

public interface EmailService {
    void sendOtpEmail(String to, String otpCode);
    void sendPasswordResetEmail(String to, String otpCode);
}
