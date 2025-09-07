package blog.sammi.lab.notes.application.mapper;

import blog.sammi.lab.notes.domain.entity.Tag;
import blog.sammi.lab.notes.presentation.dto.TagDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TagMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "noteCount", expression = "java(tag.getNotes() != null ? (long) tag.getNotes().size() : 0L)")
    TagDto toDto(Tag tag);
    
    List<TagDto> toDtoList(List<Tag> tags);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Tag toEntity(TagDto dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateTagFromDto(TagDto dto, @MappingTarget Tag tag);
}
