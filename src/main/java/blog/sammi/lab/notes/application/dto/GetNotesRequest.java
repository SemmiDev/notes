package blog.sammi.lab.notes.application.dto;

import lombok.Builder;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record GetNotesRequest(
    UUID userId,
    String search,
    UUID categoryId,
    List<UUID> tagIds,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Pageable pageable
) {}
