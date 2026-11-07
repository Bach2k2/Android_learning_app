package com.example.japaneseapp.admin.dto;

import com.example.japaneseapp.auth.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAdminUserRequest(
        @Email @NotBlank @Size(max = 255) String email,
        @NotNull UserRole role,
        @Size(max = 72) String password
) {
    @Override
    public String toString() {
        return "UpdateAdminUserRequest[email=" + email + ", role=" + role + ", password=[REDACTED]]";
    }
}
