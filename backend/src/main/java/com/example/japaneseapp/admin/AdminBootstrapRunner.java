package com.example.japaneseapp.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {
    private final AdminBootstrapService bootstrapService;
    private final AdminUserService adminUserService;
    private final String email;
    private final String password;

    public AdminBootstrapRunner(
            AdminBootstrapService bootstrapService,
            AdminUserService adminUserService,
            @Value("${ADMIN_BOOTSTRAP_EMAIL:}") String email,
            @Value("${ADMIN_BOOTSTRAP_PASSWORD:}") String password
    ) {
        this.bootstrapService = bootstrapService;
        this.adminUserService = adminUserService;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserService.hasAdministrator()) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "No administrator exists. Configure ADMIN_BOOTSTRAP_EMAIL and ADMIN_BOOTSTRAP_PASSWORD for one-time provisioning"
            );
        }
        bootstrapService.createInitialAdministrator(email, password);
    }
}
