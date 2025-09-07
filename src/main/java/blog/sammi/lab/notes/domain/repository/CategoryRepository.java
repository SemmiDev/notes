package blog.sammi.lab.notes.domain.repository;

import blog.sammi.lab.notes.domain.entity.Category;
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
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    Page<Category> findByUserId(UUID userId, Pageable pageable);
    
    Page<Category> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);
    
    List<Category> findByUserIdOrderByName(UUID userId);
    
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
    
    boolean existsByNameAndUserId(String name, UUID userId);
    
    boolean existsByNameAndUserIdAndIdNot(String name, UUID userId, UUID id);
    
    @Query("SELECT COUNT(n) FROM Note n WHERE n.category.id = :categoryId")
    long countNotesByCategoryId(@Param("categoryId") UUID categoryId);
}
