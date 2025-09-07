package blog.sammi.lab.notes.application.dto;

import lombok.Builder;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Builder
public record GetNotesByRequest(
    UUID userId,
    UUID categoryId,
    UUID tagId,
    Pageable pageable
) {}
