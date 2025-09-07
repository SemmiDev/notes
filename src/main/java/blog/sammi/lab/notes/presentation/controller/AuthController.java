package blog.sammi.lab.notes.presentation.controller;

import blog.sammi.lab.notes.application.dto.AuthResponse;
import blog.sammi.lab.notes.application.dto.LoginRequest;
import blog.sammi.lab.notes.application.dto.RegisterRequest;
import blog.sammi.lab.notes.application.usecase.AuthUseCase;
import blog.sammi.lab.notes.presentation.dto.ApiResponse;
import blog.sammi.lab.notes.presentation.dto.LoginRequestDto;
import blog.sammi.lab.notes.presentation.dto.RegisterRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and account management")
public class AuthController {
    private final AuthUseCase authUseCase;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register new user", description = "Create a new user account and send OTP verification email")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registration successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequestDto request) {
        authUseCase.register(new RegisterRequest(request.username(), request.email(), request.password()));
        return ResponseEntity.ok(ApiResponse.success("Registration successful. Please check your email for OTP."));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials or account not verified")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponse response = authUseCase.login(new LoginRequest(request.usernameOrEmail(), request.password()));
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify account using OTP code sent to email")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account verified successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Parameter(description = "User email address") @RequestParam String email,
            @Parameter(description = "6-digit OTP code") @RequestParam String otpCode) {
        authUseCase.verifyOtp(email, otpCode);
        return ResponseEntity.ok(ApiResponse.success("Account verified successfully"));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Resend OTP verification code to email")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User not found or already verified")
    })
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Parameter(description = "User email address") @RequestParam String email) {
        authUseCase.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(description = "Refresh token") @RequestParam String refreshToken) {
        AuthResponse response = authUseCase.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset OTP to email")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset OTP sent"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "User email address") @RequestParam String email) {
        authUseCase.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset OTP sent to your email"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using OTP code")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(description = "User email address") @RequestParam String email,
            @Parameter(description = "6-digit OTP code") @RequestParam String otpCode,
            @Parameter(description = "New password") @RequestParam String newPassword) {
        authUseCase.resetPassword(email, otpCode, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}
