package com.orbitflow.api.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("invalid username or password");
    }
}
