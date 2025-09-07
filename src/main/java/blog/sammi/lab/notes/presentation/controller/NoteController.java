package blog.sammi.lab.notes.presentation.controller;

import blog.sammi.lab.notes.application.dto.*;
import blog.sammi.lab.notes.application.usecase.NoteUseCase;
import blog.sammi.lab.notes.presentation.dto.ApiResponse;
import blog.sammi.lab.notes.presentation.dto.CreateNoteRequestDto;
import blog.sammi.lab.notes.presentation.dto.Meta;
import blog.sammi.lab.notes.presentation.dto.NoteDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/notes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Note management operations")
@SecurityRequirement(name = "bearerAuth")
public class NoteController {
    
    private final NoteUseCase noteUseCase;
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create note", description = "Create a new note")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Note created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category or tag not found")
    })
    public ResponseEntity<ApiResponse<NoteDto>> createNote(
            @Valid @RequestBody CreateNoteRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        CreateNoteRequest request = CreateNoteRequest.builder()
                .title(requestDto.title())
                .content(requestDto.content())
                .categoryId(requestDto.categoryId())
                .tagIds(requestDto.tagIds())
                .userId(userId)
                .build();
        
        NoteDto createdNote = noteUseCase.createNote(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Catatan berhasil dibuat", createdNote));
    }
    
    @GetMapping
    @Operation(summary = "Get notes", description = "Get paginated list of user notes with advanced filtering")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notes retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<NoteDto>>> getNotes(
            @Parameter(description = "Search term (full-text search)") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filter by tag IDs") @RequestParam(required = false) List<UUID> tagIds,
            @Parameter(description = "Filter by start date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Filter by end date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "updatedAt,desc") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetNotesRequest request = GetNotesRequest.builder()
                .userId(userId)
                .search(search)
                .categoryId(categoryId)
                .tagIds(tagIds)
                .startDate(startDate)
                .endDate(endDate)
                .pageable(pageable)
                .build();
        
        Page<NoteDto> notes = noteUseCase.getNotes(request);
        
        return ResponseEntity.ok(ApiResponse.successWithMeta(
                notes.getContent(),
                Meta.withPagination(notes)
        ));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search notes", description = "Full-text search in notes using PostgreSQL FTS")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Search query is required")
    })
    public ResponseEntity<ApiResponse<List<NoteDto>>> searchNotes(
            @Parameter(description = "Search query", required = true) @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        SearchNotesRequest request = SearchNotesRequest.builder()
                .userId(userId)
                .query(query)
                .pageable(pageable)
                .build();
        
        Page<NoteDto> notes = noteUseCase.searchNotes(request);
        
        return ResponseEntity.ok(ApiResponse.successWithMeta(
                notes.getContent(),
                Meta.withPagination(notes)
        ));
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get notes by category", description = "Get all notes in a specific category")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<List<NoteDto>>> getNotesByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId,
            @PageableDefault(size = 10, sort = "updatedAt,desc") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetNotesByRequest request = GetNotesByRequest.builder()
                .userId(userId)
                .categoryId(categoryId)
                .pageable(pageable)
                .build();
        
        Page<NoteDto> notes = noteUseCase.getNotesByCategory(request);
        
        return ResponseEntity.ok(ApiResponse.successWithMeta(
                notes.getContent(),
                Meta.withPagination(notes)
        ));
    }
    
    @GetMapping("/tag/{tagId}")
    @Operation(summary = "Get notes by tag", description = "Get all notes with a specific tag")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<ApiResponse<List<NoteDto>>> getNotesByTag(
            @Parameter(description = "Tag ID") @PathVariable UUID tagId,
            @PageableDefault(size = 10, sort = "updatedAt,desc") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetNotesByRequest request = GetNotesByRequest.builder()
                .userId(userId)
                .tagId(tagId)
                .pageable(pageable)
                .build();
        
        Page<NoteDto> notes = noteUseCase.getNotesByTag(request);
        
        return ResponseEntity.ok(ApiResponse.successWithMeta(
                notes.getContent(),
                Meta.withPagination(notes)
        ));
    }
    
    @GetMapping("/{noteId}")
    @Operation(summary = "Get note by ID", description = "Get a specific note by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found")
    })
    public ResponseEntity<ApiResponse<NoteDto>> getNoteById(
            @Parameter(description = "Note ID") @PathVariable UUID noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetCategoryByIdRequest request = GetCategoryByIdRequest.builder()
                .categoryId(noteId)
                .userId(userId)
                .build();
        
        NoteDto note = noteUseCase.getNoteById(request);
        
        return ResponseEntity.ok(ApiResponse.success("Catatan ditemukan", note));
    }
    
    @PutMapping(value = "/{noteId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update note", description = "Update an existing note")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note, category, or tag not found")
    })
    public ResponseEntity<ApiResponse<NoteDto>> updateNote(
            @Parameter(description = "Note ID") @PathVariable UUID noteId,
            @Valid @RequestBody CreateNoteRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        UpdateNoteRequest request = UpdateNoteRequest.builder()
                .noteId(noteId)
                .userId(userId)
                .title(requestDto.title())
                .content(requestDto.content())
                .categoryId(requestDto.categoryId())
                .tagIds(requestDto.tagIds())
                .build();
        
        NoteDto updatedNote = noteUseCase.updateNote(request);
        
        return ResponseEntity.ok(ApiResponse.success("Catatan berhasil diperbarui", updatedNote));
    }
    
    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete note", description = "Delete a note")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Note deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Note not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @Parameter(description = "Note ID") @PathVariable UUID noteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        DeleteCategoryRequest request = DeleteCategoryRequest.builder()
                .categoryId(noteId)
                .userId(userId)
                .build();
        
        noteUseCase.deleteNote(request);
        
        return ResponseEntity.ok(ApiResponse.success("Catatan berhasil dihapus"));
    }
}
