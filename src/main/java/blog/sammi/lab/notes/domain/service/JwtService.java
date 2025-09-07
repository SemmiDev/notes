package blog.sammi.lab.notes.domain.service;

import java.util.Date;

public interface JwtService {
    String generateToken(String username);
    String extractUsername(String token);
    Date extractExpiration(String token);
    boolean isTokenExpired(String token);
    boolean validateToken(String token, String username);
}
