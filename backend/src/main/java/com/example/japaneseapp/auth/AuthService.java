package com.example.japaneseapp.auth;

import com.example.japaneseapp.auth.dto.AuthResponse;
import com.example.japaneseapp.common.ApiException;
import com.example.japaneseapp.config.AppProperties;
import com.example.japaneseapp.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties.JwtProperties jwtProps;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProps = appProperties.security().jwt();
    }

    @Transactional
    public AuthResponse register(String email, String rawPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        var user = new AppUser(UUID.randomUUID(), email.trim().toLowerCase(), passwordEncoder.encode(rawPassword), Instant.now());
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(String email, String rawPassword) {
        var user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        var refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        return issueTokens(refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse issueTokens(AppUser user) {
        String accessToken = jwtService.createAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(jwtProps.refreshTokenTtlSeconds());

        var refreshToken = new RefreshToken(UUID.randomUUID(), user, refreshTokenValue, expiresAt, false);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenValue);
    }
}
