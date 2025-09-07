package blog.sammi.lab.notes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public record LoginRequestDto(
    @Schema(description = "Username or email address", example = "john_doe")
    @NotBlank String usernameOrEmail,
    
    @Schema(description = "Password", example = "password123")
    @NotBlank String password
) {}
