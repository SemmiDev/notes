package blog.sammi.lab.notes.presentation.controller;

import blog.sammi.lab.notes.application.dto.*;
import blog.sammi.lab.notes.application.usecase.TagUseCase;
import blog.sammi.lab.notes.presentation.dto.ApiResponse;
import blog.sammi.lab.notes.presentation.dto.Meta;
import blog.sammi.lab.notes.presentation.dto.TagDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/tags", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tag management operations")
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    
    private final TagUseCase tagUseCase;
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create tag", description = "Create a new tag")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tag created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag name already exists")
    })
    public ResponseEntity<ApiResponse<TagDto>> createTag(
            @Valid @RequestBody TagDto tagDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        CreateTagRequest request = CreateTagRequest.builder()
                .name(tagDto.name())
                .color(tagDto.color())
                .userId(userId)
                .build();
        
        TagDto createdTag = tagUseCase.createTag(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tag berhasil dibuat", createdTag));
    }
    
    @GetMapping
    @Operation(summary = "Get tags", description = "Get paginated list of user tags with optional search and color filter")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tags retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TagDto>>> getTags(
            @Parameter(description = "Search term for tag name") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by color (hex format)") @RequestParam(required = false) String color,
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetTagsRequest request = GetTagsRequest.builder()
                .userId(userId)
                .search(search)
                .color(color)
                .pageable(pageable)
                .build();
        
        Page<TagDto> tags = tagUseCase.getTags(request);
        
        return ResponseEntity.ok(ApiResponse.successWithMeta(
                tags.getContent(),
                Meta.withPagination(tags)
        ));
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all user tags", description = "Get all tags for the current user (no pagination)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All tags retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TagDto>>> getAllUserTags(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetAllUserTagsRequest request = GetAllUserTagsRequest.builder()
                .userId(userId)
                .build();
        
        List<TagDto> tags = tagUseCase.getAllUserTags(request);
        
        return ResponseEntity.ok(ApiResponse.success("Semua tag berhasil diambil", tags));
    }
    
    @GetMapping("/{tagId}")
    @Operation(summary = "Get tag by ID", description = "Get a specific tag by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<ApiResponse<TagDto>> getTagById(
            @Parameter(description = "Tag ID") @PathVariable UUID tagId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetCategoryByIdRequest request = GetCategoryByIdRequest.builder()
                .categoryId(tagId)
                .userId(userId)
                .build();
        
        TagDto tag = tagUseCase.getTagById(request);
        
        return ResponseEntity.ok(ApiResponse.success("Tag ditemukan", tag));
    }
    
    @PutMapping(value = "/{tagId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update tag", description = "Update an existing tag")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag name already exists")
    })
    public ResponseEntity<ApiResponse<TagDto>> updateTag(
            @Parameter(description = "Tag ID") @PathVariable UUID tagId,
            @Valid @RequestBody TagDto tagDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        UpdateTagRequest request = UpdateTagRequest.builder()
                .tagId(tagId)
                .userId(userId)
                .name(tagDto.name())
                .color(tagDto.color())
                .build();
        
        TagDto updatedTag = tagUseCase.updateTag(request);
        
        return ResponseEntity.ok(ApiResponse.success("Tag berhasil diperbarui", updatedTag));
    }
    
    @DeleteMapping("/{tagId}")
    @Operation(summary = "Delete tag", description = "Delete a tag (only if no notes are associated)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag has associated notes")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @Parameter(description = "Tag ID") @PathVariable UUID tagId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        DeleteCategoryRequest request = DeleteCategoryRequest.builder()
                .categoryId(tagId)
                .userId(userId)
                .build();
        
        tagUseCase.deleteTag(request);
        
        return ResponseEntity.ok(ApiResponse.success("Tag berhasil dihapus"));
    }
}
