package com.orbitflow.api.dto.response;

public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
