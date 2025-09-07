package blog.sammi.lab.notes.domain.repository;

import blog.sammi.lab.notes.domain.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    
    Page<Note> findByUserId(UUID userId, Pageable pageable);
    
    Page<Note> findByUserIdAndCategoryId(UUID userId, UUID categoryId, Pageable pageable);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE n.user.id = :userId AND t.id = :tagId")
    Page<Note> findByUserIdAndTagsId(@Param("userId") UUID userId, @Param("tagId") UUID tagId, Pageable pageable);
    
    Optional<Note> findByIdAndUserId(UUID id, UUID userId);
    
    // Full-text search using PostgreSQL tsvector
    @Query(value = """
        SELECT n.* FROM notes n 
        WHERE n.user_id = :userId 
        AND n.search_vector @@ to_tsquery('indonesian', :query)
        ORDER BY ts_rank(n.search_vector, to_tsquery('indonesian', :query)) DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM notes n 
        WHERE n.user_id = :userId 
        AND n.search_vector @@ to_tsquery('indonesian', :query)
        """,
        nativeQuery = true)
    Page<Note> fullTextSearch(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);
    
    // Advanced search with filters
    @Query("""
        SELECT n FROM Note n 
        WHERE n.user.id = :userId
        AND n.searchVector @@ function('to_tsquery', 'indonesian', :query)
        AND (:categoryId IS NULL OR n.category.id = :categoryId)
        AND (:#{#tagIds == null || #tagIds.isEmpty()} = true OR EXISTS (
            SELECT t FROM n.tags t WHERE t.id IN :tagIds
        ))
        AND (:startDate IS NULL OR n.createdAt >= :startDate)
        AND (:endDate IS NULL OR n.createdAt <= :endDate)
        ORDER BY function('ts_rank', n.searchVector, function('to_tsquery', 'indonesian', :query)) DESC
        """)
    Page<Note> searchNotes(
        @Param("userId") UUID userId,
        @Param("query") String query,
        @Param("categoryId") UUID categoryId,
        @Param("tagIds") List<UUID> tagIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // Filter without search
    @Query("""
        SELECT n FROM Note n 
        WHERE n.user.id = :userId
        AND (:categoryId IS NULL OR n.category.id = :categoryId)
        AND (:#{#tagIds == null || #tagIds.isEmpty()} = true OR EXISTS (
            SELECT t FROM n.tags t WHERE t.id IN :tagIds
        ))
        AND (:startDate IS NULL OR n.createdAt >= :startDate)
        AND (:endDate IS NULL OR n.createdAt <= :endDate)
        ORDER BY n.updatedAt DESC, n.createdAt DESC
        """)
    Page<Note> findNotesWithFilters(
        @Param("userId") UUID userId,
        @Param("categoryId") UUID categoryId,
        @Param("tagIds") List<UUID> tagIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
