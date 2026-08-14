package com.orbitflow.api.service;

import com.orbitflow.api.dto.request.StepDefinitionRequest;
import com.orbitflow.api.exception.InvalidWorkflowDefinitionException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DagResolutionService {

    public void validateDefinition(List<StepDefinitionRequest> steps) {
        Set<String> names = steps.stream().map(StepDefinitionRequest::name).collect(Collectors.toSet());
        if (names.size() != steps.size()) {
            throw new InvalidWorkflowDefinitionException("step names must be unique within a workflow");
        }

        for (StepDefinitionRequest step : steps) {
            for (String dep : dependsOnOf(step)) {
                if (dep.equals(step.name())) {
                    throw new InvalidWorkflowDefinitionException(
                        "step '%s' cannot depend on itself".formatted(step.name()));
                }
                if (!names.contains(dep)) {
                    throw new InvalidWorkflowDefinitionException(
                        "step '%s' depends on unknown step '%s'".formatted(step.name(), dep));
                }
            }
        }

        detectCycle(steps);
    }

    private void detectCycle(List<StepDefinitionRequest> steps) {
        Map<String, List<String>> graph = new HashMap<>();
        for (StepDefinitionRequest step : steps) {
            graph.put(step.name(), dependsOnOf(step));
        }

        Set<String> visited = new HashSet<>();
        Set<String> inProgress = new HashSet<>();
        for (String node : graph.keySet()) {
            if (!visited.contains(node) && hasCycle(node, graph, visited, inProgress)) {
                throw new InvalidWorkflowDefinitionException("workflow definition contains a dependency cycle");
            }
        }
    }

    private boolean hasCycle(String node, Map<String, List<String>> graph, Set<String> visited, Set<String> inProgress) {
        visited.add(node);
        inProgress.add(node);

        for (String dep : graph.getOrDefault(node, List.of())) {
            if (inProgress.contains(dep)) {
                return true;
            }
            if (!visited.contains(dep) && hasCycle(dep, graph, visited, inProgress)) {
                return true;
            }
        }

        inProgress.remove(node);
        return false;
    }

    private List<String> dependsOnOf(StepDefinitionRequest step) {
        return step.dependsOn() == null ? List.of() : step.dependsOn();
    }
}
