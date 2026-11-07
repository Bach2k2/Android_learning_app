package com.example.japaneseapp.security;

import com.example.japaneseapp.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final AppProperties.JwtProperties props;
    private final SecretKey key;

    public JwtService(AppProperties appProperties) {
        this.props = appProperties.security().jwt();
        var secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes (256-bit) for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String createAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessTokenTtlSeconds());
        return Jwts.builder()
                .issuer(props.issuer())
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of("email", email, "typ", "access"))
                .signWith(key)
                .compact();
    }

    public JwtClaims parseAndValidate(String jwt) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        String subject = claims.getSubject();
        String email = claims.get("email", String.class);
        String typ = claims.get("typ", String.class);
        return new JwtClaims(UUID.fromString(subject), email, typ);
    }

    public record JwtClaims(UUID userId, String email, String type) {
    }
}
