package blog.sammi.lab.notes.domain.repository;

import blog.sammi.lab.notes.domain.entity.RefreshToken;
import blog.sammi.lab.notes.domain.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    RefreshToken save(RefreshToken refreshToken);
    void delete(RefreshToken refreshToken);
}
