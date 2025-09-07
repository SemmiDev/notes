package blog.sammi.lab.notes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Create note request")
public record CreateNoteRequest(
    @NotBlank(message = "Judul catatan tidak boleh kosong")
    @Size(min = 1, max = 255, message = "Judul harus antara 1-255 karakter")
    @Schema(description = "Note title", example = "My Important Note", required = true)
    String title,
    
    @Schema(description = "Note content", example = "This is the content of my note...")
    String content,
    
    @Schema(description = "Category ID")
    UUID categoryId,
    
    @Schema(description = "List of tag IDs")
    List<UUID> tagIds
) {}
