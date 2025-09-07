package blog.sammi.lab.notes.application.dto;

import lombok.Builder;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Builder
public record SearchNotesRequest(
    UUID userId,
    String query,
    Pageable pageable
) {}
