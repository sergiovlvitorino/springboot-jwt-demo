package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjwt.application.service.RefreshTokenService;

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
import java.util.Map;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTLoginFilter.class);

    private final TokenAuthenticationService tokenAuthenticationService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JWTLoginFilter(String url, AuthenticationManager authManager,
                          TokenAuthenticationService tokenAuthenticationService,
                          RefreshTokenService refreshTokenService,
                          LoginAttemptService loginAttemptService) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
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
            loginAttemptService.loginFailed(accountCredentials.username());
            throw e;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res,
                                            FilterChain chain, Authentication auth) throws IOException, ServletException {
        String clientIp = req.getRemoteAddr();
        log.info("Login successful for user: {} from IP: {}", PiiMasker.maskEmail(auth.getName()), clientIp);
        loginAttemptService.loginSucceeded(auth.getName());

        tokenAuthenticationService.addAuthentication(res, auth.getName());

        String accessToken = res.getHeader(TokenAuthenticationService.HEADER_STRING);
        if (accessToken != null && accessToken.startsWith(TokenAuthenticationService.TOKEN_PREFIX + " ")) {
            accessToken = accessToken.substring(TokenAuthenticationService.TOKEN_PREFIX.length() + 1);
        }

        String refreshTokenValue = null;
        if (auth.getPrincipal() instanceof UserDetailsAdapter adapter) {
            refreshTokenValue = refreshTokenService.createRefreshToken(adapter.getUser().getId());
        }

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        Map<String, String> body = new java.util.LinkedHashMap<>();
        body.put("accessToken", accessToken);
        body.put("refreshToken", refreshTokenValue);
        objectMapper.writeValue(res.getWriter(), body);
    }

}
