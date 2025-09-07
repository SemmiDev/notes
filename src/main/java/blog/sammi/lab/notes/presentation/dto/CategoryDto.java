package blog.sammi.lab.notes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Category data transfer object")
public record CategoryDto(
    @Schema(description = "Category ID") 
    UUID id,
    
    @NotBlank(message = "Nama kategori tidak boleh kosong")
    @Size(min = 1, max = 100, message = "Nama kategori harus antara 1-100 karakter")
    @Schema(description = "Category name", example = "Work", required = true)
    String name,
    
    @Size(max = 500, message = "Deskripsi maksimal 500 karakter")
    @Schema(description = "Category description", example = "Work related notes")
    String description,
    
    @Schema(description = "User ID who owns this category")
    UUID userId,
    
    @Schema(description = "Number of notes in this category")
    Long noteCount,
    
    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,
    
    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt
) {}
