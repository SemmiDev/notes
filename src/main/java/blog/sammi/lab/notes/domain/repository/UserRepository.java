package blog.sammi.lab.notes.domain.repository;

import blog.sammi.lab.notes.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User save(User user);
    Optional<User> findById(UUID id);
}
