package blog.sammi.lab.notes.application.port.in;

import blog.sammi.lab.notes.presentation.dto.NoteDto;

import java.util.List;
import java.util.UUID;

/**
 * Inbound Port - Defines what the application can do (Use Cases)
 * This interface represents the contract for note management operations
 * that can be triggered from the outside (controllers, CLI, etc.)
 */
public interface NoteManagementUseCase {
    
    /**
     * Create a new note
     */
    NoteDto createNote(CreateNoteCommand command);
    
    /**
     * Update an existing note
     */
    NoteDto updateNote(UpdateNoteCommand command);
    
    /**
     * Delete a note
     */
    void deleteNote(UUID noteId, UUID userId);
    
    /**
     * Get note by ID
     */
    NoteDto getNoteById(UUID noteId, UUID userId);
    
    /**
     * Get all notes for a user
     */
    List<NoteDto> getUserNotes(UUID userId, int page, int size);
    
    /**
     * Search notes by title or content
     */
    List<NoteDto> searchNotes(String query, UUID userId, int page, int size);
    
    /**
     * Share note with another user
     */
    void shareNote(UUID noteId, UUID ownerId, UUID targetUserId);
}
