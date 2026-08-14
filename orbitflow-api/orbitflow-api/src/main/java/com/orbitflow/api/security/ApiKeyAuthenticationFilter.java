package com.orbitflow.api.security;

import com.orbitflow.api.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Service-to-service auth: an X-API-Key header maps to a tenant, authenticated as ROLE_SERVICE.
 * Runs before the JWT resource-server filter; a request only ever carries one credential type.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String key = request.getHeader(HEADER);
        if (key != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            apiKeyRepository.findByKeyHashAndActiveTrue(ApiKeyHasher.hash(key)).ifPresent(apiKey -> {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_SERVICE"));
                var authentication = new UsernamePasswordAuthenticationToken(apiKey.getTenant(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}
