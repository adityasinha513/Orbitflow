package com.orbitflow.api.controller;

import com.orbitflow.api.dto.request.CreateWorkflowRequest;
import com.orbitflow.api.dto.response.StepDefinitionResponse;
import com.orbitflow.api.dto.response.WorkflowResponse;
import com.orbitflow.api.entity.Workflow;
import com.orbitflow.api.entity.WorkflowStepDefinition;
import com.orbitflow.api.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowResponse createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        Workflow workflow = workflowService.createWorkflow(request);
        return toResponse(workflow);
    }

    private WorkflowResponse toResponse(Workflow workflow) {
        return new WorkflowResponse(
            workflow.getId(),
            workflow.getName(),
            workflow.getCreatedAt(),
            workflow.getSteps().stream()
                .map(this::toStepResponse)
                .toList()
        );
    }

    private StepDefinitionResponse toStepResponse(WorkflowStepDefinition def) {
        return new StepDefinitionResponse(def.getStepName(), def.getCallbackUrl(), def.getDependsOn());
    }
}
