package com.example.japaneseapp.admin;

import com.example.japaneseapp.auth.AppUser;
import com.example.japaneseapp.auth.PasswordPolicy;
import com.example.japaneseapp.auth.UserRepository;
import com.example.japaneseapp.auth.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminBootstrapService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createInitialAdministrator(String configuredEmail, String rawPassword) {
        if (userRepository.countByRole(UserRole.ADMIN) > 0) {
            return;
        }

        String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
        if (email.length() > 255 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_EMAIL must be a valid email address");
        }
        if (!PasswordPolicy.meetsAdminMinimum(rawPassword)) {
            throw new IllegalStateException(
                    "ADMIN_BOOTSTRAP_PASSWORD must be at least 12 characters and no more than 72 UTF-8 bytes"
            );
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException(
                    "The bootstrap email already belongs to a non-admin account; provision the administrator explicitly"
            );
        }

        var administrator = new AppUser(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(rawPassword),
                Instant.now(),
                UserRole.ADMIN
        );
        userRepository.save(administrator);
    }
}
