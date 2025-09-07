package blog.sammi.lab.notes.application.port.in;

import java.util.Set;
import java.util.UUID;

/**
 * Command object for creating a note
 * Encapsulates all data needed for the create note use case
 */
public record CreateNoteCommand(
    String title,
    String content,
    UUID userId,
    UUID categoryId,
    Set<UUID> tagIds
) {
    public CreateNoteCommand {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
