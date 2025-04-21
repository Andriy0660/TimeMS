package com.example.timecraft.domain.user.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.audit.service.AuditService;
import com.example.timecraft.domain.multitenant.config.TenantIdentifierResolver;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionServiceImpl implements UserDeletionService {
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public void deleteUserAndTenant(Long userId) {
        log.info("Видалення користувача з ID: {}", userId);

        // Перевіряємо, чи існує користувач
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Користувача з ID " + userId + " не знайдено"));

        // Запам'ятовуємо інформацію про користувача для логування
        String userEmail = user.getEmail();

        try {
            // Встановлюємо поточний тенант як public для виконання системних операцій
            TenantIdentifierResolver.setCurrentTenant("public");

            // Логуємо дію (перед видаленням користувача)
            auditService.logAction(
                null, // користувач буде видалений, тому null
                "USER_DELETION",
                "USER",
                userId.toString(),
                "Видалення користувача: " + userEmail,
                null
            );

            jdbcTemplate.queryForList("SELECT * FROM delete_user_and_tenant(?)", userId);

            log.info("Користувач {} успішно видалений разом з його тенантами", userEmail);
        } catch (Exception e) {
            log.error("Помилка при видаленні користувача з ID: {}", userId, e);
            throw new RuntimeException("Помилка при видаленні користувача: " + e.getMessage(), e);
        }
    }
}