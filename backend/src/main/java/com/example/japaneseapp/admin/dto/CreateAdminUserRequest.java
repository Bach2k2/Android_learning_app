package com.example.japaneseapp.admin.dto;

import com.example.japaneseapp.auth.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAdminUserRequest(
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotNull UserRole role
) {
    @Override
    public String toString() {
        return "CreateAdminUserRequest[email=" + email + ", password=[REDACTED], role=" + role + "]";
    }
}
