package Camaras.VIDEOCAMARAS.infraestructure.security;

import org.springframework.security.core.Authentication;

public interface JwtGenerator {
    String generateToken(Authentication authentication);
    String refreshToken(Authentication authentication);
    String getUsernameFromJWT(String token);
    boolean validateToken(String token);
}
