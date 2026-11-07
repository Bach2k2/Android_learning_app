package com.example.japaneseapp.admin;

import com.example.japaneseapp.admin.dto.CreateAdminUserRequest;
import com.example.japaneseapp.admin.dto.AdminUserPageResponse;
import com.example.japaneseapp.admin.dto.AdminUserResponse;
import com.example.japaneseapp.admin.dto.UpdateAdminUserRequest;
import com.example.japaneseapp.auth.AppUser;
import com.example.japaneseapp.auth.PasswordPolicy;
import com.example.japaneseapp.auth.RefreshTokenRepository;
import com.example.japaneseapp.auth.UserRepository;
import com.example.japaneseapp.auth.UserRole;
import com.example.japaneseapp.common.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static com.example.japaneseapp.admin.dto.AdminUserResponse.from;

@Service
public class AdminUserService {
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public AdminUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(readOnly = true)
    public AdminUserPageResponse listUsers(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid page or page size");
        }

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));
        Page<AdminUserResponse> users =
                userRepository.findAll(pageable).map(from);
        return AdminUserPageResponse.from(users);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(UUID id) {
        return from(findUser(id));
    }

    @Transactional
    public AdminUserResponse createUser(CreateAdminUserRequest request) {
        if (request.role() == UserRole.ADMIN) {
            userRepository.findAllByRoleForUpdate(UserRole.ADMIN);
        }
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        requireAdminPassword(request.password());

        var user = new AppUser(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(request.password()),
                Instant.now(),
                request.role()
        );
        return from(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse updateUser(
            UUID id,
            UpdateAdminUserRequest request
    ) {
        var lockedAdmins = userRepository.findAllByRoleForUpdate(UserRole.ADMIN);
        AppUser user = findUser(id);
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (user.getRole() == UserRole.ADMIN && request.role() != UserRole.ADMIN) {
            ensureAnotherAdminRemains(lockedAdmins);
        }

        boolean invalidateSessions = user.getRole() != request.role();
        String replacementPassword = request.password();
        if (replacementPassword != null && !replacementPassword.isBlank()) {
            requireAdminPassword(replacementPassword);
            user.updatePasswordHash(passwordEncoder.encode(replacementPassword));
            invalidateSessions = true;
        }

        user.updateEmail(email);
        user.updateRole(request.role());
        if (invalidateSessions) {
            refreshTokenRepository.revokeAllActiveByUserId(id);
        }
        return from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id, UUID actingAdminId) {
        if (id.equals(actingAdminId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Administrators cannot delete their own account");
        }

        var lockedAdmins = userRepository.findAllByRoleForUpdate(UserRole.ADMIN);
        AppUser user = findUser(id);
        if (user.getRole() == UserRole.ADMIN) {
            ensureAnotherAdminRemains(lockedAdmins);
        }
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean hasAdministrator() {
        return userRepository.countByRole(UserRole.ADMIN) > 0;
    }

    private AppUser findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void ensureAnotherAdminRemains(java.util.List<AppUser> lockedAdmins) {
        if (lockedAdmins.size() <= 1) {
            throw new ApiException(HttpStatus.CONFLICT, "The last administrator cannot be removed or demoted");
        }
    }

    private void requireAdminPassword(String password) {
        if (!PasswordPolicy.meetsAdminMinimum(password)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least 12 characters and no more than 72 UTF-8 bytes"
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
