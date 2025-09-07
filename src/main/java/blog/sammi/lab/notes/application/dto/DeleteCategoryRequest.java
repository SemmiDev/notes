package blog.sammi.lab.notes.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DeleteCategoryRequest(
    UUID categoryId,
    UUID userId
) {}
