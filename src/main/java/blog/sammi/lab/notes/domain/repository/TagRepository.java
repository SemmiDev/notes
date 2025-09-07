package blog.sammi.lab.notes.domain.repository;

import blog.sammi.lab.notes.domain.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    
    Page<Tag> findByUserId(UUID userId, Pageable pageable);
    
    Page<Tag> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);
    
    Page<Tag> findByUserIdAndColor(UUID userId, String color, Pageable pageable);
    
    Page<Tag> findByUserIdAndNameContainingIgnoreCaseAndColor(UUID userId, String name, String color, Pageable pageable);
    
    List<Tag> findByUserIdOrderByName(UUID userId);
    
    Optional<Tag> findByIdAndUserId(UUID id, UUID userId);
    
    boolean existsByNameAndUserId(String name, UUID userId);
    
    boolean existsByNameAndUserIdAndIdNot(String name, UUID userId, UUID id);
    
    @Query("SELECT COUNT(n) FROM Note n JOIN n.tags t WHERE t.id = :tagId")
    long countNotesByTagId(@Param("tagId") UUID tagId);
}
