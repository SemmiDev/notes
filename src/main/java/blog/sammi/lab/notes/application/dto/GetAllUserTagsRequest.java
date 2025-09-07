package blog.sammi.lab.notes.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record GetAllUserTagsRequest(
    UUID userId
) {}
