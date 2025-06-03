package Camaras.VIDEOCAMARAS.infraestructure.security;

import Camaras.VIDEOCAMARAS.shared.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtGeneratorImpl implements JwtGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JwtGeneratorImpl.class);

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Override
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return createToken(userDetails.getUsername(), extractAuthorities(userDetails));
    }

    @Override
    public String refreshToken(Authentication authentication) {
        return generateToken(authentication); // reutiliza generación
    }

    private String createToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plus(SecurityConstants.JWT_EXPIRATION);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getKey())
                .compact();
    }

    private List<String> extractAuthorities(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private Key getKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String getUsernameFromJWT(String token) {
        return getClaims(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Error al validar el token: {}", e.getMessage());
            throw new JwtAuthenticationException("Token inválido: " + e.getMessage(), e);
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Si necesitas exponer getClaims, agrégalo a la interfaz también.
    public <T> T getClaims(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(getAllClaims(token));
    }
}
