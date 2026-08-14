package com.orbitflow.api.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "workflow_step_definitions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"workflow_id", "step_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStepDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "workflow_step_dependencies",
        joinColumns = @JoinColumn(name = "step_definition_id")
    )
    @Column(name = "depends_on_step_name")
    @Builder.Default
    private Set<String> dependsOn = new HashSet<>();
}
