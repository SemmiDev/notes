package blog.sammi.lab.notes.application.usecase;

import blog.sammi.lab.notes.application.dto.*;
import blog.sammi.lab.notes.domain.entity.RefreshToken;
import blog.sammi.lab.notes.domain.entity.User;
import blog.sammi.lab.notes.domain.repository.RefreshTokenRepository;
import blog.sammi.lab.notes.domain.repository.UserRepository;
import blog.sammi.lab.notes.domain.service.EmailService;
import blog.sammi.lab.notes.domain.service.JwtService;
import blog.sammi.lab.notes.domain.service.PasswordEncoder;
import blog.sammi.lab.notes.domain.service.QueueService;
import blog.sammi.lab.notes.infrastructure.config.StructuredLogger;
import blog.sammi.lab.notes.presentation.dto.ErrorCode;
import blog.sammi.lab.notes.presentation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthUseCase {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final StructuredLogger structuredLogger;
    private final QueueService queueService;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Starting user registration for email: {}", request.email());

        long startTime = System.currentTimeMillis();

        try {
            if (userRepository.existsByEmail(request.email())) {
                structuredLogger.logSecurityEvent("REGISTRATION_ATTEMPT_DUPLICATE_EMAIL",
                    request.username(), "Email already exists: " + request.email());
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }

            if (userRepository.existsByUsername(request.username())) {
                structuredLogger.logSecurityEvent("REGISTRATION_ATTEMPT_DUPLICATE_USERNAME",
                    request.username(), "Username already exists: " + request.username());
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
            }

            User user = new User();
            user.setUsername(request.username());
            user.setEmail(request.email());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setOtpCode(generateOtp());
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

            userRepository.save(user);

            // Queue email sending instead of synchronous call
            Map<String, Object> emailPayload = new HashMap<>();
            emailPayload.put("type", "OTP_VERIFICATION");
            emailPayload.put("to", user.getEmail());
            emailPayload.put("otpCode", user.getOtpCode());
            queueService.enqueue("email", "EMAIL", emailPayload);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", request.username());
            eventData.put("email", request.email());
            structuredLogger.logBusinessEvent("USER_REGISTRATION", "SUCCESS", eventData);

            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("email", request.email());
            structuredLogger.logPerformanceMetric("USER_REGISTRATION", duration, metrics);

            log.info("User registration completed successfully for email: {}", request.email());

        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("email", request.email());
            context.put("username", request.username());
            structuredLogger.logError("USER_REGISTRATION", e, context);
            throw e;
        }
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Starting login attempt for user: {}", request.usernameOrEmail());

        long startTime = System.currentTimeMillis();

        try {
            User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                    .orElseThrow(() -> {
                        structuredLogger.logSecurityEvent("LOGIN_ATTEMPT_USER_NOT_FOUND",
                            request.usernameOrEmail(), "User not found");
                        return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                    });

            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                structuredLogger.logSecurityEvent("LOGIN_ATTEMPT_INVALID_PASSWORD",
                    user.getUsername(), "Invalid password attempt");
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }

            if (!user.isVerified()) {
                structuredLogger.logSecurityEvent("LOGIN_ATTEMPT_UNVERIFIED_ACCOUNT",
                    user.getUsername(), "Account not verified");
                throw new BusinessException(ErrorCode.ACCOUNT_NOT_VERIFIED);
            }

            if (!user.isActive()) {
                structuredLogger.logSecurityEvent("LOGIN_ATTEMPT_INACTIVE_ACCOUNT",
                    user.getUsername(), "Account is disabled");
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
            }

            String accessToken = jwtService.generateToken(user.getUsername());
            RefreshToken refreshToken = createRefreshToken(user);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", user.getUsername());
            eventData.put("email", user.getEmail());
            structuredLogger.logBusinessEvent("USER_LOGIN", "SUCCESS", eventData);

            long duration = System.currentTimeMillis() - startTime;
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("username", user.getUsername());
            structuredLogger.logPerformanceMetric("USER_LOGIN", duration, metrics);

            log.info("Login completed successfully for user: {}", user.getUsername());

            return AuthResponse.of(accessToken, refreshToken.getToken(), 3600000L);

        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("usernameOrEmail", request.usernameOrEmail());
            structuredLogger.logError("USER_LOGIN", e, context);
            throw e;
        }
    }

    @Transactional
    public void verifyOtp(String email, String otpCode) {
        log.info("Starting OTP verification for email: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            if (user.getOtpCode() == null || !user.getOtpCode().equals(otpCode)) {
                structuredLogger.logSecurityEvent("OTP_VERIFICATION_INVALID",
                    user.getUsername(), "Invalid OTP code");
                throw new BusinessException(ErrorCode.INVALID_OTP);
            }

            if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
                structuredLogger.logSecurityEvent("OTP_VERIFICATION_EXPIRED",
                    user.getUsername(), "OTP expired");
                throw new BusinessException(ErrorCode.OTP_EXPIRED);
            }

            user.setVerified(true);
            user.setActive(true);
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", user.getUsername());
            eventData.put("email", email);
            structuredLogger.logBusinessEvent("OTP_VERIFICATION", "SUCCESS", eventData);

            log.info("OTP verification completed successfully for email: {}", email);

        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("email", email);
            structuredLogger.logError("OTP_VERIFICATION", e, context);
            throw e;
        }
    }

    @Transactional
    public void resendOtp(String email) {
        log.info("Resending OTP for email: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            if (user.isVerified()) {
                throw new BusinessException(ErrorCode.ACCOUNT_NOT_VERIFIED, "Akun sudah diverifikasi");
            }

            user.setOtpCode(generateOtp());
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            emailService.sendOtpEmail(user.getEmail(), user.getOtpCode());

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", user.getUsername());
            eventData.put("email", email);
            structuredLogger.logBusinessEvent("OTP_RESEND", "SUCCESS", eventData);

            log.info("OTP resent successfully for email: {}", email);

        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("email", email);
            structuredLogger.logError("OTP_RESEND", e, context);
            throw e;
        }
    }

    public AuthResponse refreshToken(String refreshTokenValue) {
        log.info("Starting token refresh");

        try {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                    .orElseThrow(() -> {
                        structuredLogger.logSecurityEvent("TOKEN_REFRESH_INVALID",
                            "unknown", "Invalid refresh token");
                        return new BusinessException(ErrorCode.INVALID_TOKEN);
                    });

            if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                refreshTokenRepository.delete(refreshToken);
                structuredLogger.logSecurityEvent("TOKEN_REFRESH_EXPIRED",
                    refreshToken.getUser().getUsername(), "Refresh token expired");
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }

            String accessToken = jwtService.generateToken(refreshToken.getUser().getUsername());

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", refreshToken.getUser().getUsername());
            structuredLogger.logBusinessEvent("TOKEN_REFRESH", "SUCCESS", eventData);

            log.info("Token refresh completed successfully for user: {}", refreshToken.getUser().getUsername());

            return AuthResponse.of(accessToken, refreshToken.getToken(), 3600000L);

        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("refreshToken", refreshTokenValue.substring(0, 8) + "...");
            structuredLogger.logError("TOKEN_REFRESH", e, context);
            throw e;
        }
    }

    @Transactional
    public void forgotPassword(String email) {
        log.info("Starting forgot password for email: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            user.setResetToken(generateOtp());
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getResetToken());

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", user.getUsername());
            eventData.put("email", email);
            structuredLogger.logBusinessEvent("PASSWORD_RESET_REQUEST", "SUCCESS", eventData);

            log.info("Password reset email sent successfully for email: {}", email);

        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("email", email);
            structuredLogger.logError("PASSWORD_RESET_REQUEST", e, context);
            throw e;
        }
    }

    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword) {
        log.info("Starting password reset for email: {}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            if (user.getResetToken() == null || !user.getResetToken().equals(otpCode)) {
                structuredLogger.logSecurityEvent("PASSWORD_RESET_INVALID_TOKEN",
                    user.getUsername(), "Invalid reset token");
                throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
            }

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                structuredLogger.logSecurityEvent("PASSWORD_RESET_EXPIRED_TOKEN",
                    user.getUsername(), "Reset token expired");
                throw new BusinessException(ErrorCode.RESET_TOKEN_EXPIRED);
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);

            refreshTokenRepository.deleteByUser(user);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("username", user.getUsername());
            eventData.put("email", email);
            structuredLogger.logBusinessEvent("PASSWORD_RESET", "SUCCESS", eventData);

            log.info("Password reset completed successfully for email: {}", email);

        } catch (Exception e) {
            Map<String, Object> context = new HashMap<>();
            context.put("email", email);
            structuredLogger.logError("PASSWORD_RESET", e, context);
            throw e;
        }
    }

    @Transactional
    protected RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        return refreshTokenRepository.save(refreshToken);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
