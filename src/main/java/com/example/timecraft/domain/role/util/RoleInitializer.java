package com.example.timecraft.domain.role.util;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.domain.role.persistence.RoleEntity;
import com.example.timecraft.domain.role.persistence.RoleRepository;
import com.example.timecraft.domain.user.persistence.UserEntity;
import com.example.timecraft.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Перевіряємо, чи є користувачі без ролей
        List<UserEntity> usersWithoutRoles = userRepository.findAll().stream()
            .filter(user -> user.getRoles() == null || user.getRoles().isEmpty())
            .toList();

        if (usersWithoutRoles.isEmpty()) {
            log.info("All users have roles assigned.");
            return;
        }

        log.info("Found {} users without roles. Assigning default ROLE_USER.", usersWithoutRoles.size());

        // Отримуємо роль USER
        RoleEntity userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Призначаємо роль всім користувачам без ролей
        for (UserEntity user : usersWithoutRoles) {
            user.setRoles(List.of(userRole));
            user.setActive(true); // Встановлюємо активність за замовчуванням
            userRepository.save(user);
            log.info("Assigned ROLE_USER to user: {}", user.getEmail());
        }
    }
}