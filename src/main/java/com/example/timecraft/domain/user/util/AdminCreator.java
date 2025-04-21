package com.example.timecraft.domain.user.util;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.domain.audit.service.AuditService;
import com.example.timecraft.domain.multitenant.persistence.TenantEntity;
import com.example.timecraft.domain.multitenant.service.MultiTenantService;
import com.example.timecraft.domain.role.persistence.RoleEntity;
import com.example.timecraft.domain.role.persistence.RoleRepository;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.example.timecraft.domain.multitenant.util.MultiTenantUtils.generateSchemaNameFromEmail;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminCreator implements ApplicationRunner {
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin1";
    private static final String ADMIN_FIRST_NAME = "Admin";
    private static final String ADMIN_LAST_NAME = "Admin";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MultiTenantService multiTenantService;
    private final AuditService auditService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Optional<UserEntity> existingAdmin = userRepository.findByEmail(ADMIN_EMAIL);

        if (existingAdmin.isPresent()) {
            log.info("Admin user already exists: {}", ADMIN_EMAIL);

            // Переконуємося, що адміністратор має роль ADMIN
            UserEntity admin = existingAdmin.get();
            RoleEntity adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

            if (admin.getRoles() == null || admin.getRoles().isEmpty() ||
                !admin.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {

                log.info("Adding ROLE_ADMIN to existing admin user");
                admin.setRoles(List.of(adminRole));
                admin.setActive(true);
                userRepository.save(admin);
            }

            return;
        }

        log.info("Creating admin user: {}", ADMIN_EMAIL);

        // Отримуємо роль ADMIN
        RoleEntity adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("Admin role not found"));

        // Створюємо адміністратора
        UserEntity admin = UserEntity.builder()
            .email(ADMIN_EMAIL)
            .password(passwordEncoder.encode(ADMIN_PASSWORD))
            .firstName(ADMIN_FIRST_NAME)
            .lastName(ADMIN_LAST_NAME)
            .isActive(true)
            .roles(List.of(adminRole))
            .build();

        // Створюємо тенант для адміністратора
        TenantEntity adminTenant = multiTenantService.createTenant(generateSchemaNameFromEmail(admin.getEmail()));
        admin.getTenants().add(adminTenant);

        UserEntity savedAdmin = userRepository.save(admin);

        log.info("Admin user created successfully: {}", ADMIN_EMAIL);

        // Логуємо створення адміністратора
        auditService.logAction(
            "ADMIN_CREATION",
            "USER",
            savedAdmin.getId().toString(),
            "Admin user created automatically"
        );
    }
}