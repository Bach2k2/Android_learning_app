package com.example.japaneseapp.admin;

import com.example.japaneseapp.auth.AppUser;
import com.example.japaneseapp.auth.UserRepository;
import com.example.japaneseapp.auth.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBootstrapServiceTest {
    @Test
    void createsFirstAdministratorWithEncodedPassword() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AdminBootstrapService service = new AdminBootstrapService(userRepository, passwordEncoder);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(0L);
        when(userRepository.existsByEmailIgnoreCase("admin@example.test")).thenReturn(false);
        when(passwordEncoder.encode("an-initial-admin-password")).thenReturn("bcrypt-hash");

        service.createInitialAdministrator("Admin@Example.Test", "an-initial-admin-password");

        var savedUser = org.mockito.ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("admin@example.test", savedUser.getValue().getEmail());
        assertEquals(UserRole.ADMIN, savedUser.getValue().getRole());
        assertEquals("bcrypt-hash", savedUser.getValue().getPasswordHash());
        verify(passwordEncoder).encode("an-initial-admin-password");
    }

    @Test
    void doesNotRecreateOrReplaceAdministratorWhenOneExists() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AdminBootstrapService service = new AdminBootstrapService(userRepository, passwordEncoder);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        service.createInitialAdministrator("admin@example.test", "an-initial-admin-password");

        verify(userRepository, never()).save(any(AppUser.class));
        verifyNoInteractions(passwordEncoder);
    }
}
