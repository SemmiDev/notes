package blog.sammi.lab.notes.application.usecase;

import blog.sammi.lab.notes.application.dto.*;
import blog.sammi.lab.notes.application.mapper.NoteMapper;
import blog.sammi.lab.notes.domain.entity.Category;
import blog.sammi.lab.notes.domain.entity.Note;
import blog.sammi.lab.notes.domain.entity.Tag;
import blog.sammi.lab.notes.domain.entity.User;
import blog.sammi.lab.notes.domain.repository.CategoryRepository;
import blog.sammi.lab.notes.domain.repository.NoteRepository;
import blog.sammi.lab.notes.domain.repository.TagRepository;
import blog.sammi.lab.notes.domain.repository.UserRepository;
import blog.sammi.lab.notes.presentation.dto.ErrorCode;
import blog.sammi.lab.notes.presentation.dto.NoteDto;
import blog.sammi.lab.notes.presentation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteUseCase {
    
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final NoteMapper noteMapper;
    
    public NoteDto createNote(CreateNoteRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Note note = new Note();
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setUser(user);
        
        // Set category if provided
        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.categoryId(), request.userId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            note.setCategory(category);
        }
        
        // Set tags if provided
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (var tagId : request.tagIds()) {
                Tag tag = tagRepository.findByIdAndUserId(tagId, request.userId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
                tags.add(tag);
            }
            note.setTags(tags);
        }
        
        Note savedNote = noteRepository.save(note);
        return noteMapper.toDto(savedNote);
    }
    
    public Page<NoteDto> getNotes(GetNotesRequest request) {
        Page<Note> notes;
        
        if (request.search() != null && !request.search().trim().isEmpty()) {
            // Full-text search using PostgreSQL
            notes = noteRepository.searchNotes(
                    request.userId(), 
                    request.search().trim(), 
                    request.categoryId(), 
                    request.tagIds(), 
                    request.startDate(), 
                    request.endDate(), 
                    request.pageable()
            );
        } else {
            // Regular filtering
            notes = noteRepository.findNotesWithFilters(
                    request.userId(), 
                    request.categoryId(), 
                    request.tagIds(), 
                    request.startDate(), 
                    request.endDate(), 
                    request.pageable()
            );
        }
        
        return notes.map(noteMapper::toDto);
    }
    
    public NoteDto getNoteById(GetCategoryByIdRequest request) {
        Note note = noteRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND));
        
        return noteMapper.toDto(note);
    }
    
    public NoteDto updateNote(UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(request.noteId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND));
        
        note.setTitle(request.title());
        note.setContent(request.content());
        
        // Update category
        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.categoryId(), request.userId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            note.setCategory(category);
        } else {
            note.setCategory(null);
        }
        
        // Update tags
        note.getTags().clear();
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (var tagId : request.tagIds()) {
                Tag tag = tagRepository.findByIdAndUserId(tagId, request.userId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
                tags.add(tag);
            }
            note.setTags(tags);
        }
        
        Note savedNote = noteRepository.save(note);
        return noteMapper.toDto(savedNote);
    }
    
    public void deleteNote(DeleteCategoryRequest request) {
        Note note = noteRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND));
        
        noteRepository.delete(note);
    }
    
    @Transactional(readOnly = true)
    public Page<NoteDto> searchNotes(SearchNotesRequest request) {
        if (request.query() == null || request.query().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Query pencarian tidak boleh kosong");
        }
        
        Page<Note> notes = noteRepository.fullTextSearch(request.userId(), request.query().trim(), request.pageable());
        return notes.map(noteMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<NoteDto> getNotesByCategory(GetNotesByRequest request) {
        // Verify category belongs to user
        categoryRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        
        Page<Note> notes = noteRepository.findByUserIdAndCategoryId(request.userId(), request.categoryId(), request.pageable());
        return notes.map(noteMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<NoteDto> getNotesByTag(GetNotesByRequest request) {
        // Verify tag belongs to user
        tagRepository.findByIdAndUserId(request.tagId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
        
        Page<Note> notes = noteRepository.findByUserIdAndTagsId(request.userId(), request.tagId(), request.pageable());
        return notes.map(noteMapper::toDto);
    }
}
