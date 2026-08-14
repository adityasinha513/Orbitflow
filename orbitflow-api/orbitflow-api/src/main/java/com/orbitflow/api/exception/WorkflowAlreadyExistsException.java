package com.orbitflow.api.exception;

public class WorkflowAlreadyExistsException extends RuntimeException {

    public WorkflowAlreadyExistsException(String message) {
        super(message);
    }
}
