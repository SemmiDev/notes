package blog.sammi.lab.notes.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateTagRequest(
    UUID tagId,
    UUID userId,
    
    @NotBlank(message = "Nama tag tidak boleh kosong")
    @Size(min = 1, max = 50, message = "Nama tag harus antara 1-50 karakter")
    String name,
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Warna harus dalam format hex (#RRGGBB)")
    String color
) {}
