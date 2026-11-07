package com.example.japaneseapp.admin;

import com.example.japaneseapp.admin.dto.CreateAdminUserRequest;
import com.example.japaneseapp.admin.dto.UpdateAdminUserRequest;
import com.example.japaneseapp.auth.AppUser;
import com.example.japaneseapp.auth.RefreshTokenRepository;
import com.example.japaneseapp.auth.UserRepository;
import com.example.japaneseapp.auth.UserRole;
import com.example.japaneseapp.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminUserServiceTest {
    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenRepository refreshTokenRepository;
    private AdminUserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        service = new AdminUserService(userRepository, passwordEncoder, refreshTokenRepository);
    }

    @Test
    void createUserHashesPasswordAndReturnsOnlySafeFields() {
        String rawPassword = "a-strong-admin-password";
        when(userRepository.existsByEmailIgnoreCase("new@example.test")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn("bcrypt-hash");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createUser(
                new CreateAdminUserRequest("New@Example.Test", rawPassword, UserRole.USER));

        ArgumentCaptor<AppUser> savedUser = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("new@example.test", savedUser.getValue().getEmail());
        assertEquals("bcrypt-hash", savedUser.getValue().getPasswordHash());
        assertEquals(UserRole.USER, savedUser.getValue().getRole());
        assertEquals("new@example.test", response.email());
        assertEquals(UserRole.USER, response.role());
        assertFalse(response.toString().contains("bcrypt-hash"));
        assertFalse(response.toString().contains(rawPassword));
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void updateWithBlankPasswordLeavesExistingHashUnchanged() {
        AppUser user = new AppUser(USER_ID, "user@example.test", "existing-hash", Instant.now(), UserRole.USER);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("user@example.test", USER_ID)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        service.updateUser(USER_ID, new UpdateAdminUserRequest("user@example.test", UserRole.USER, " "));

        assertEquals("existing-hash", user.getPasswordHash());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void passwordResetHashesNewPasswordAndRevokesRefreshTokens() {
        String newPassword = "another-long-password";
        AppUser user = new AppUser(USER_ID, "user@example.test", "existing-hash", Instant.now(), UserRole.USER);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("user@example.test", USER_ID)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("new-bcrypt-hash");
        when(userRepository.save(user)).thenReturn(user);

        service.updateUser(
                USER_ID,
                new UpdateAdminUserRequest("user@example.test", UserRole.USER, newPassword)
        );

        assertEquals("new-bcrypt-hash", user.getPasswordHash());
        verify(refreshTokenRepository).revokeAllActiveByUserId(USER_ID);
    }

    @Test
    void rejectsPasswordExceedingBcryptUtf8Limit() {
        String password = "あ".repeat(25);
        when(userRepository.existsByEmailIgnoreCase("user@example.test")).thenReturn(false);

        ApiException error = assertThrows(ApiException.class, () -> service.createUser(
                new CreateAdminUserRequest("user@example.test", password, UserRole.USER)
        ));

        assertEquals(400, error.getStatus().value());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void rejectsDemotionOfLastAdministrator() {
        AppUser admin = new AppUser(ADMIN_ID, "admin@example.test", "hash", Instant.now(), UserRole.ADMIN);
        when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("admin@example.test", ADMIN_ID)).thenReturn(false);
        when(userRepository.findAllByRoleForUpdate(UserRole.ADMIN)).thenReturn(List.of(admin));

        ApiException error = assertThrows(ApiException.class, () -> service.updateUser(
                ADMIN_ID,
                new UpdateAdminUserRequest("admin@example.test", UserRole.USER, null)
        ));

        assertEquals(409, error.getStatus().value());
        assertEquals(UserRole.ADMIN, admin.getRole());
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void rejectsDeletingOwnAccount() {
        ApiException error = assertThrows(ApiException.class, () -> service.deleteUser(ADMIN_ID, ADMIN_ID));

        assertEquals(409, error.getStatus().value());
        verify(userRepository, never()).delete(any(AppUser.class));
    }
}
