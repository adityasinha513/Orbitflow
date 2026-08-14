package com.orbitflow.api.repository;

import com.orbitflow.api.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByName(String name);

    boolean existsByName(String name);
}
