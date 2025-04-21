package com.example.timecraft.domain.multitenant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.example.timecraft.domain.multitenant.service.SchemaManagerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class SchemaManagerConfig {
    private final SchemaManagerService schemaManagerService;

    @PostConstruct
    public void initializeDatabase() {
        log.info("Initializing database schemas");

        schemaManagerService.initializeMainSchema();

        log.info("Database schema initialization completed");
    }
}