package blog.sammi.lab.notes.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "User data transfer object")
public record UserDto(
    @Schema(description = "User ID") UUID id,
    @Schema(description = "Username") String username,
    @Schema(description = "Email address") String email,
    @Schema(description = "Account active status") boolean active,
    @Schema(description = "Account verified status") boolean verified,
    @Schema(description = "Account creation timestamp") LocalDateTime createdAt,
    @Schema(description = "Last update timestamp") LocalDateTime updatedAt
) {}
