package blog.sammi.lab.notes.application.usecase;

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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TagUseCase {
    
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;
    
    public TagDto createTag(TagDto tagDto, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // Check if tag name already exists for this user
        if (tagRepository.existsByNameAndUserId(tagDto.name(), userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Tag dengan nama tersebut sudah ada");
        }
        
        Tag tag = tagMapper.toEntity(tagDto);
        tag.setUser(user);
        
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDto(savedTag);
    }
    
    public Page<TagDto> getTags(UUID userId, String search, String color, Pageable pageable) {
        Page<Tag> tags;
        
        if (search != null && !search.trim().isEmpty()) {
            if (color != null && !color.trim().isEmpty()) {
                tags = tagRepository.findByUserIdAndNameContainingIgnoreCaseAndColor(userId, search.trim(), color.trim(), pageable);
            } else {
                tags = tagRepository.findByUserIdAndNameContainingIgnoreCase(userId, search.trim(), pageable);
            }
        } else if (color != null && !color.trim().isEmpty()) {
            tags = tagRepository.findByUserIdAndColor(userId, color.trim(), pageable);
        } else {
            tags = tagRepository.findByUserId(userId, pageable);
        }
        
        return tags.map(tagMapper::toDto);
    }
    
    public List<TagDto> getAllUserTags(UUID userId) {
        List<Tag> tags = tagRepository.findByUserIdOrderByName(userId);
        return tagMapper.toDtoList(tags);
    }
    
    public TagDto getTagById(UUID tagId, UUID userId) {
        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
        
        return tagMapper.toDto(tag);
    }
    
    public TagDto updateTag(UUID tagId, TagDto tagDto, UUID userId) {
        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
        
        // Check if new name conflicts with existing tags (excluding current one)
        if (!tag.getName().equals(tagDto.name()) && 
            tagRepository.existsByNameAndUserIdAndIdNot(tagDto.name(), userId, tagId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "Tag dengan nama tersebut sudah ada");
        }
        
        tagMapper.updateTagFromDto(tagDto, tag);
        Tag savedTag = tagRepository.save(tag);
        
        return tagMapper.toDto(savedTag);
    }
    
    public void deleteTag(UUID tagId, UUID userId) {
        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
        
        // Check if tag is used by notes
        if (tagRepository.countNotesByTagId(tagId) > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, 
                "Tidak dapat menghapus tag yang masih digunakan oleh catatan");
        }
        
        tagRepository.delete(tag);
    }
}
