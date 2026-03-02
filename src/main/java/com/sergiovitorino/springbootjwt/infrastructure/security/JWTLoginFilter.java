package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTLoginFilter.class);

    private TokenAuthenticationService tokenAuthenticationService;

    public JWTLoginFilter(String url, AuthenticationManager authManager, TokenAuthenticationService tokenAuthenticationService) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException, IOException {
        final var accountCredentials = new ObjectMapper().readValue(req.getInputStream(), AccountCredentials.class);
        String clientIp = getClientIp(req);
        log.debug("Login attempt for user: {} from IP: {}", accountCredentials.username(), clientIp);

        try {
            return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(accountCredentials.username(), accountCredentials.password(), Collections.emptyList())
            );
        } catch (AuthenticationException e) {
            log.warn("Login failed for user: {} from IP: {} - Reason: {}", accountCredentials.username(), clientIp, e.getMessage());
            throw e;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) {
        String clientIp = getClientIp(req);
        log.info("Login successful for user: {} from IP: {}", auth.getName(), clientIp);
        tokenAuthenticationService.addAuthentication(res, auth.getName());
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
