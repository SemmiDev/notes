package blog.sammi.lab.notes.application.mapper;

import blog.sammi.lab.notes.domain.entity.Category;
import blog.sammi.lab.notes.presentation.dto.CategoryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "noteCount", expression = "java(category.getNotes() != null ? (long) category.getNotes().size() : 0L)")
    CategoryDto toDto(Category category);
    
    List<CategoryDto> toDtoList(List<Category> categories);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Category toEntity(CategoryDto dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateCategoryFromDto(CategoryDto dto, @MappingTarget Category category);
}
