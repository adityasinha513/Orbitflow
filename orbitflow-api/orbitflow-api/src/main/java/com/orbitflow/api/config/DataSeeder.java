package com.orbitflow.api.config;

import com.orbitflow.api.entity.ApiKey;
import com.orbitflow.api.entity.AppUser;
import com.orbitflow.api.entity.Role;
import com.orbitflow.api.repository.ApiKeyRepository;
import com.orbitflow.api.repository.AppUserRepository;
import com.orbitflow.api.security.ApiKeyHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * No signup or key-issuance endpoints exist yet, so seed one admin user and one demo API key
 * on first boot. Purely a local-dev/demo bootstrap - a real deployment would provision these
 * out of band, not on every startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${orbitflow.security.seed.admin-username:admin}")
    private String adminUsername;

    @Value("${orbitflow.security.seed.admin-password:admin123}")
    private String adminPassword;

    @Value("${orbitflow.security.seed.demo-api-key:orbitflow_demo_key_12345}")
    private String demoApiKey;

    @Value("${orbitflow.security.seed.demo-tenant:acme-payments}")
    private String demoTenant;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!appUserRepository.existsByUsername(adminUsername)) {
            appUserRepository.save(AppUser.builder()
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build());
            log.info("seeded admin user '{}' / '{}' (override via ORBITFLOW_SECURITY_SEED_ADMIN_USERNAME / _PASSWORD)",
                adminUsername, adminPassword);
        }

        if (!apiKeyRepository.existsByTenant(demoTenant)) {
            apiKeyRepository.save(ApiKey.builder()
                .keyHash(ApiKeyHasher.hash(demoApiKey))
                .tenant(demoTenant)
                .active(true)
                .createdAt(Instant.now())
                .build());
            log.info("seeded demo API key for tenant '{}': {}", demoTenant, demoApiKey);
        }
    }
}
