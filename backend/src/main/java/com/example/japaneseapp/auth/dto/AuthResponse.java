package com.example.japaneseapp.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
