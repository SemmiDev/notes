package blog.sammi.lab.notes.application.port.out;

import blog.sammi.lab.notes.domain.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port - Defines what the application needs from the persistence layer
 * This interface represents the contract for note data access
 * that the application depends on (database, file system, etc.)
 */
public interface NoteRepositoryPort {
    
    /**
     * Save a note
     */
    Note save(Note note);
    
    /**
     * Find note by ID and user ID (for security)
     */
    Optional<Note> findByIdAndUserId(UUID noteId, UUID userId);
    
    /**
     * Find all notes for a user with pagination
     */
    Page<Note> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Search notes by title or content
     */
    Page<Note> searchByTitleOrContentAndUserId(String query, UUID userId, Pageable pageable);
    
    /**
     * Find notes by category
     */
    List<Note> findByCategoryIdAndUserId(UUID categoryId, UUID userId);
    
    /**
     * Find notes by tag
     */
    List<Note> findByTagsIdAndUserId(UUID tagId, UUID userId);
    
    /**
     * Delete note by ID and user ID
     */
    void deleteByIdAndUserId(UUID noteId, UUID userId);
    
    /**
     * Check if note exists and belongs to user
     */
    boolean existsByIdAndUserId(UUID noteId, UUID userId);
    
    /**
     * Count notes for a user
     */
    long countByUserId(UUID userId);
}
