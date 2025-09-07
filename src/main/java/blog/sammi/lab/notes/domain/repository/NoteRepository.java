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
    
    // Simple text search using native SQL
    @Query(value = """
        SELECT n.* FROM notes n 
        WHERE n.user_id = :userId 
        AND (n.title ILIKE CONCAT('%', :query, '%') OR n.content ILIKE CONCAT('%', :query, '%'))
        ORDER BY n.updated_at DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM notes n 
        WHERE n.user_id = :userId 
        AND (n.title ILIKE CONCAT('%', :query, '%') OR n.content ILIKE CONCAT('%', :query, '%'))
        """,
        nativeQuery = true)
    Page<Note> fullTextSearch(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);
    
    // Advanced search with filters using native SQL
    @Query(value = """
        SELECT DISTINCT n.* FROM notes n 
        LEFT JOIN note_tags nt ON n.id = nt.note_id
        WHERE n.user_id = :userId
        AND (:query IS NULL OR n.title ILIKE CONCAT('%', :query, '%') OR n.content ILIKE CONCAT('%', :query, '%'))
        AND (:categoryId IS NULL OR n.category_id = :categoryId)
        AND (:#{#tagIds == null || #tagIds.isEmpty()} = true OR nt.tag_id IN :tagIds)
        AND (:startDate IS NULL OR n.created_at >= :startDate)
        AND (:endDate IS NULL OR n.created_at <= :endDate)
        ORDER BY n.updated_at DESC, n.created_at DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT n.id) FROM notes n 
        LEFT JOIN note_tags nt ON n.id = nt.note_id
        WHERE n.user_id = :userId
        AND (:query IS NULL OR n.title ILIKE CONCAT('%', :query, '%') OR n.content ILIKE CONCAT('%', :query, '%'))
        AND (:categoryId IS NULL OR n.category_id = :categoryId)
        AND (:#{#tagIds == null || #tagIds.isEmpty()} = true OR nt.tag_id IN :tagIds)
        AND (:startDate IS NULL OR n.created_at >= :startDate)
        AND (:endDate IS NULL OR n.created_at <= :endDate)
        """,
        nativeQuery = true)
    Page<Note> searchNotes(
        @Param("userId") UUID userId,
        @Param("query") String query,
        @Param("categoryId") UUID categoryId,
        @Param("tagIds") List<UUID> tagIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // Filter without search using JPQL
    @Query("""
        SELECT DISTINCT n FROM Note n 
        LEFT JOIN n.tags t
        WHERE n.user.id = :userId
        AND (:categoryId IS NULL OR n.category.id = :categoryId)
        AND (:#{#tagIds == null || #tagIds.isEmpty()} = true OR t.id IN :tagIds)
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
