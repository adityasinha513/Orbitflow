package com.orbitflow.api.service;

import com.orbitflow.api.dto.request.CreateWorkflowRequest;
import com.orbitflow.api.dto.request.StepDefinitionRequest;
import com.orbitflow.api.entity.Workflow;
import com.orbitflow.api.entity.WorkflowStepDefinition;
import com.orbitflow.api.exception.WorkflowAlreadyExistsException;
import com.orbitflow.api.exception.WorkflowNotFoundException;
import com.orbitflow.api.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final DagResolutionService dagResolutionService;

    @Transactional
    public Workflow createWorkflow(CreateWorkflowRequest request) {
        if (workflowRepository.existsByName(request.name())) {
            throw new WorkflowAlreadyExistsException("workflow '%s' already exists".formatted(request.name()));
        }

        dagResolutionService.validateDefinition(request.steps());

        Workflow workflow = Workflow.builder().name(request.name()).build();

        List<WorkflowStepDefinition> definitions = request.steps().stream()
            .map(step -> toDefinition(workflow, step))
            .toList();
        workflow.setSteps(definitions);

        return workflowRepository.save(workflow);
    }

    @Transactional(readOnly = true)
    public Workflow getByName(String name) {
        return workflowRepository.findByName(name)
            .orElseThrow(() -> new WorkflowNotFoundException("workflow '%s' not found".formatted(name)));
    }

    private WorkflowStepDefinition toDefinition(Workflow workflow, StepDefinitionRequest step) {
        return WorkflowStepDefinition.builder()
            .workflow(workflow)
            .stepName(step.name())
            .callbackUrl(step.callbackUrl())
            .dependsOn(step.dependsOn() == null ? new HashSet<>() : new HashSet<>(step.dependsOn()))
            .build();
    }
}
