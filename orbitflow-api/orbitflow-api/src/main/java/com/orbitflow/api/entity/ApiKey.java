package com.orbitflow.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** keyHash is a SHA-256 digest of the key - the plaintext key is only ever shown once, at creation. */
@Entity
@Table(name = "api_keys", uniqueConstraints = @UniqueConstraint(columnNames = "key_hash"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_hash", nullable = false, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private String tenant;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at")
    private Instant createdAt;
}
