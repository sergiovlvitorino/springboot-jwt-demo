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
        String clientIp = req.getRemoteAddr();
        log.debug("Login attempt from IP: {}", clientIp);

        try {
            return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(accountCredentials.username(), accountCredentials.password(), Collections.emptyList())
            );
        } catch (AuthenticationException e) {
            log.warn("Login failed from IP: {} - Reason: {}", clientIp, e.getMessage());
            throw e;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) {
        String clientIp = req.getRemoteAddr();
        log.info("Login successful for user: {} from IP: {}", maskEmail(auth.getName()), clientIp);
        tokenAuthenticationService.addAuthentication(res, auth.getName());
    }

    static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "*" + email.substring(atIndex);
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
