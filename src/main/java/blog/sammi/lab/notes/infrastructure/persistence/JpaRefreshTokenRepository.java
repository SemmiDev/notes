package blog.sammi.lab.notes.infrastructure.persistence;

import blog.sammi.lab.notes.domain.entity.RefreshToken;
import blog.sammi.lab.notes.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
