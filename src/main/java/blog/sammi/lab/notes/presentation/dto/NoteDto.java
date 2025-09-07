package blog.sammi.lab.notes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Note data transfer object")
public record NoteDto(
    @Schema(description = "Note ID") 
    UUID id,
    
    @NotBlank(message = "Judul catatan tidak boleh kosong")
    @Size(min = 1, max = 255, message = "Judul harus antara 1-255 karakter")
    @Schema(description = "Note title", example = "My Important Note", required = true)
    String title,
    
    @Schema(description = "Note content", example = "This is the content of my note...")
    String content,
    
    @Schema(description = "User ID who owns this note")
    UUID userId,
    
    @Schema(description = "Category information")
    CategoryDto category,
    
    @Schema(description = "List of tags")
    List<TagDto> tags,
    
    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,
    
    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt
) {}
