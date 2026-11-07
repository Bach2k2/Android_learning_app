package com.example.japaneseapp.admin;

import com.example.japaneseapp.admin.dto.AdminDashboardResponse;
import com.example.japaneseapp.admin.dto.AdminUserPageResponse;
import com.example.japaneseapp.admin.dto.AdminUserResponse;
import com.example.japaneseapp.admin.dto.CreateAdminUserRequest;
import com.example.japaneseapp.admin.dto.UpdateAdminUserRequest;
import com.example.japaneseapp.lesson.LessonRepository;
import com.example.japaneseapp.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminUserService adminUserService;
    private final LessonRepository lessonRepository;

    public AdminController(AdminUserService adminUserService, LessonRepository lessonRepository) {
        this.adminUserService = adminUserService;
        this.lessonRepository = lessonRepository;
    }

    @GetMapping("/me")
    public AdminUserResponse currentAdministrator() {
        return adminUserService.getUser(SecurityUtils.requireUserId());
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(lessonRepository.count(), adminUserService.countUsers());
    }

    @GetMapping("/users")
    public AdminUserPageResponse listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminUserService.listUsers(page, size);
    }

    @PostMapping("/users")
    public ResponseEntity<AdminUserResponse> createUser(
            @Valid @RequestBody CreateAdminUserRequest request
    ) {
        AdminUserResponse user = adminUserService.createUser(request);
        return ResponseEntity.created(URI.create("/api/admin/users/" + user.id())).body(user);
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUser(@PathVariable UUID id) {
        return adminUserService.getUser(id);
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAdminUserRequest request
    ) {
        return adminUserService.updateUser(id, request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminUserService.deleteUser(id, SecurityUtils.requireUserId());
        return ResponseEntity.noContent().build();
    }
}
