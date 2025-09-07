package blog.sammi.lab.notes.application.port.in;

import java.util.Set;
import java.util.UUID;

/**
 * Command object for updating a note
 * Encapsulates all data needed for the update note use case
 */
public record UpdateNoteCommand(
    UUID noteId,
    String title,
    String content,
    UUID userId,
    UUID categoryId,
    Set<UUID> tagIds
) {
    public UpdateNoteCommand {
        if (noteId == null) {
            throw new IllegalArgumentException("Note ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}
