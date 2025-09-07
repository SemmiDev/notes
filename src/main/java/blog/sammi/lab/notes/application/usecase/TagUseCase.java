package blog.sammi.lab.notes.application.usecase;

import blog.sammi.lab.notes.application.dto.*;
import blog.sammi.lab.notes.application.mapper.TagMapper;
import blog.sammi.lab.notes.domain.entity.Tag;
import blog.sammi.lab.notes.domain.entity.User;
import blog.sammi.lab.notes.domain.repository.TagRepository;
import blog.sammi.lab.notes.domain.repository.UserRepository;
import blog.sammi.lab.notes.presentation.dto.ErrorCode;
import blog.sammi.lab.notes.presentation.dto.TagDto;
import blog.sammi.lab.notes.presentation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TagUseCase {
    
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;
    
    public TagDto createTag(CreateTagRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // Check if tag name already exists for this user
        if (tagRepository.existsByNameAndUserId(request.name(), request.userId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Tag dengan nama tersebut sudah ada");
        }
        
        Tag tag = new Tag();
        tag.setName(request.name());
        tag.setColor(request.color());
        tag.setUser(user);
        
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }
    
    public Page<TagDto> getTags(GetTagsRequest request) {
        Page<Tag> tags;
        
        if (request.search() != null && !request.search().trim().isEmpty()) {
            if (request.color() != null && !request.color().trim().isEmpty()) {
                tags = tagRepository.findByUserIdAndNameContainingIgnoreCaseAndColor(
                        request.userId(), request.search().trim(), request.color().trim(), request.pageable());
            } else {
                tags = tagRepository.findByUserIdAndNameContainingIgnoreCase(
                        request.userId(), request.search().trim(), request.pageable());
            }
        } else if (request.color() != null && !request.color().trim().isEmpty()) {
            tags = tagRepository.findByUserIdAndColor(request.userId(), request.color().trim(), request.pageable());
        } else {
            tags = tagRepository.findByUserId(request.userId(), request.pageable());
        }
        
        return tags.map(tagMapper::toDto);
    }
    
    public List<TagDto> getAllUserTags(GetAllUserTagsRequest request) {
        List<Tag> tags = tagRepository.findByUserIdOrderByName(request.userId());
        return tagMapper.toDtoList(tags);
    }
    
    public TagDto getTagById(GetCategoryByIdRequest request) {
        Tag tag = tagRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
        
        return tagMapper.toDto(tag);
    }
    
    public TagDto updateTag(UpdateTagRequest request) {
        Tag tag = tagRepository.findByIdAndUserId(request.tagId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
        
        // Check if new name conflicts with existing tags (excluding current one)
        if (!tag.getName().equals(request.name()) && 
            tagRepository.existsByNameAndUserIdAndIdNot(request.name(), request.userId(), request.tagId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Tag dengan nama tersebut sudah ada");
        }
        
        tag.setName(request.name());
        tag.setColor(request.color());
        
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }
    
    public void deleteTag(DeleteCategoryRequest request) {
        Tag tag = tagRepository.findByIdAndUserId(request.categoryId(), request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
        
        // Check if tag is used by notes
        if (tagRepository.countNotesByTagId(request.categoryId()) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, 
                "Tidak dapat menghapus tag yang masih digunakan oleh catatan");
        }
        
        tagRepository.delete(tag);
    }
}
