package com.orbitflow.api.service;

import com.orbitflow.api.dto.request.LoginRequest;
import com.orbitflow.api.dto.response.LoginResponse;
import com.orbitflow.api.entity.AppUser;
import com.orbitflow.api.exception.InvalidCredentialsException;
import com.orbitflow.api.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${orbitflow.security.jwt.expiration-minutes:60}")
    private long expirationMinutes;

    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username())
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.issueToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, "Bearer", expirationMinutes * 60);
    }
}
