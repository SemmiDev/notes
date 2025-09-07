package blog.sammi.lab.notes.presentation.controller;

import blog.sammi.lab.notes.application.dto.*;
import blog.sammi.lab.notes.application.usecase.CategoryUseCase;
import blog.sammi.lab.notes.presentation.dto.ApiResponse;
import blog.sammi.lab.notes.presentation.dto.CategoryDto;
import blog.sammi.lab.notes.presentation.dto.Meta;
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
@RequestMapping(value = "/api/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management operations")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    
    private final CategoryUseCase categoryUseCase;
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create category", description = "Create a new category")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name(categoryDto.name())
                .description(categoryDto.description())
                .userId(userId)
                .build();
        
        CategoryDto createdCategory = categoryUseCase.createCategory(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kategori berhasil dibuat", createdCategory));
    }
    
    @GetMapping
    @Operation(summary = "Get categories", description = "Get paginated list of user categories with optional search")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories(
            @Parameter(description = "Search term for category name") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetCategoriesRequest request = GetCategoriesRequest.builder()
                .userId(userId)
                .search(search)
                .pageable(pageable)
                .build();
        
        Page<CategoryDto> categories = categoryUseCase.getCategories(request);
        
        return ResponseEntity.ok(ApiResponse.successWithMeta(
                categories.getContent(),
                Meta.withPagination(categories)
        ));
    }
    
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Get a specific category by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        GetCategoryByIdRequest request = GetCategoryByIdRequest.builder()
                .categoryId(categoryId)
                .userId(userId)
                .build();
        
        CategoryDto category = categoryUseCase.getCategoryById(request);
        
        return ResponseEntity.ok(ApiResponse.success("Kategori ditemukan", category));
    }
    
    @PutMapping(value = "/{categoryId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update category", description = "Update an existing category")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryDto categoryDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .categoryId(categoryId)
                .userId(userId)
                .name(categoryDto.name())
                .description(categoryDto.description())
                .build();
        
        CategoryDto updatedCategory = categoryUseCase.updateCategory(request);
        
        return ResponseEntity.ok(ApiResponse.success("Kategori berhasil diperbarui", updatedCategory));
    }
    
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category", description = "Delete a category (only if no notes are associated)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category has associated notes")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        DeleteCategoryRequest request = DeleteCategoryRequest.builder()
                .categoryId(categoryId)
                .userId(userId)
                .build();
        
        categoryUseCase.deleteCategory(request);
        
        return ResponseEntity.ok(ApiResponse.success("Kategori berhasil dihapus"));
    }
}
