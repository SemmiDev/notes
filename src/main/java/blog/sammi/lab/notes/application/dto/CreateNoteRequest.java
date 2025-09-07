package blog.sammi.lab.notes.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CreateNoteRequest(
    @NotBlank(message = "Judul catatan tidak boleh kosong")
    @Size(min = 1, max = 255, message = "Judul harus antara 1-255 karakter")
    String title,
    
    String content,
    UUID categoryId,
    List<UUID> tagIds,
    UUID userId
) {}
