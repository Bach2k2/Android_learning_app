package com.example.japaneseapp.admin.dto;

import com.example.japaneseapp.auth.AppUser;
import com.example.japaneseapp.auth.UserRole;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        UserRole role,
        Instant createdAt
) {
    public static AdminUserResponse from(AppUser user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
