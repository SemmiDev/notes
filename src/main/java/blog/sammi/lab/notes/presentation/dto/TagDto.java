package blog.sammi.lab.notes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Tag data transfer object")
public record TagDto(
    @Schema(description = "Tag ID") 
    UUID id,
    
    @NotBlank(message = "Nama tag tidak boleh kosong")
    @Size(min = 1, max = 50, message = "Nama tag harus antara 1-50 karakter")
    @Schema(description = "Tag name", example = "Important", required = true)
    String name,
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Warna harus dalam format hex (#RRGGBB)")
    @Schema(description = "Tag color in hex format", example = "#ff0000")
    String color,
    
    @Schema(description = "User ID who owns this tag")
    UUID userId,
    
    @Schema(description = "Number of notes with this tag")
    Long noteCount,
    
    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,
    
    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt
) {}
