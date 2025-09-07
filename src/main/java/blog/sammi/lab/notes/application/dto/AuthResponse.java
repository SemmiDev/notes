package blog.sammi.lab.notes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response with JWT tokens")
public record AuthResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    
    @Schema(description = "Refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    String refreshToken,
    
    @Schema(description = "Token type", example = "Bearer")
    String tokenType,
    
    @Schema(description = "Token expiration time in milliseconds", example = "3600000")
    Long expiresIn
) {
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
