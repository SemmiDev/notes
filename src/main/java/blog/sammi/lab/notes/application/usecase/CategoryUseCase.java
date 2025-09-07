package blog.sammi.lab.notes.application.usecase;

import blog.sammi.lab.notes.application.dto.*;
import blog.sammi.lab.notes.application.mapper.CategoryMapper;
import blog.sammi.lab.notes.domain.entity.Category;
import blog.sammi.lab.notes.domain.entity.User;
import blog.sammi.lab.notes.domain.repository.CategoryRepository;
import blog.sammi.lab.notes.domain.repository.UserRepository;
import blog.sammi.lab.notes.presentation.dto.CategoryDto;
import blog.sammi.lab.notes.presentation.dto.ErrorCode;
import blog.sammi.lab.notes.presentation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryUseCase {
    
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    
    public CategoryDto createCategory(CreateCategoryRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // Check if category name already exists for this user
        if (categoryRepository.existsByNameAndUserId(request.name(), request.userId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Kategori dengan nama tersebut sudah ada");
        }
        
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .user(user)
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }
    
    public Page<CategoryDto> getCategories(GetCategoriesRequest request) {
        Page<Category> categories;
        
        if (request.search() != null && !request.search().trim().isEmpty()) {
            categories = categoryRepository.findByUserIdAndNameContainingIgnoreCase(
                    request.userId(), request.search().trim(), request.pageable());
        } else {
            categories = categoryRepository.findByUserId(request.userId(), request.pageable());
        }
        
        return categories.map(categoryMapper::toDto);
    }
    
    public CategoryDto getCategoryById(GetCategoryByIdRequest request) {
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        
        return categoryMapper.toDto(category);
    }
    
    public CategoryDto updateCategory(UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        
        // Check if new name conflicts with existing categories (excluding current one)
        if (!category.getName().equals(request.name()) && 
            categoryRepository.existsByNameAndUserIdAndIdNot(request.name(), request.userId(), request.categoryId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Kategori dengan nama tersebut sudah ada");
        }
        
        category.setName(request.name());
        category.setDescription(request.description());
        
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }
    
    public void deleteCategory(DeleteCategoryRequest request) {
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        
        // Check if category has notes
        if (categoryRepository.countNotesByCategoryId(request.categoryId()) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, 
                "Tidak dapat menghapus kategori yang masih memiliki catatan");
        }
        
        categoryRepository.delete(category);
    }
}
