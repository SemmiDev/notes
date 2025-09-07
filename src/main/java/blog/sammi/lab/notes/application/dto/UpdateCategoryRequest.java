package blog.sammi.lab.notes.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateCategoryRequest(
    UUID categoryId,
    UUID userId,
    
    @NotBlank(message = "Nama kategori tidak boleh kosong")
    @Size(min = 1, max = 100, message = "Nama kategori harus antara 1-100 karakter")
    String name,
    
    @Size(max = 500, message = "Deskripsi maksimal 500 karakter")
    String description
) {}
