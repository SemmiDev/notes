package blog.sammi.lab.notes.infrastructure.persistence;

import blog.sammi.lab.notes.domain.entity.RefreshToken;
import blog.sammi.lab.notes.domain.entity.User;
import blog.sammi.lab.notes.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token);
    }

    @Override
    public void deleteByUser(User user) {
        jpaRefreshTokenRepository.deleteByUser(user);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpaRefreshTokenRepository.save(refreshToken);
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        jpaRefreshTokenRepository.delete(refreshToken);
    }
}
