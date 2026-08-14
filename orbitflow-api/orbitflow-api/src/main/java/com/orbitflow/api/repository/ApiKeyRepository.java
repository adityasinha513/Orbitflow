package com.orbitflow.api.repository;

import com.orbitflow.api.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);

    boolean existsByTenant(String tenant);
}
