package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.exception.InvalidRefreshTokenException;
import com.sergiovitorino.springbootjwt.domain.model.RefreshToken;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RefreshTokenRepository;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenAuthenticationService tokenAuthenticationService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               TokenAuthenticationService tokenAuthenticationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Transactional
    public String createRefreshToken(UUID userId) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L);
        var refreshToken = new RefreshToken(tokenValue, userId, expiresAt);
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created for userId={}", userId);
        return tokenValue;
    }

    @Transactional
    public RefreshResult refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (refreshToken.isUsed()) {
            log.warn("Attempt to reuse refresh token: tokenId={}", refreshToken.getId());
            throw new InvalidRefreshTokenException("Refresh token has already been used");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Expired refresh token used: tokenId={}", refreshToken.getId());
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        User user = userRepository.findByIdWithAuthorities(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("User not found for refresh token"));

        if (!user.isEnabled()) {
            log.warn("Disabled user attempted refresh: userId={}", user.getId());
            throw new InvalidRefreshTokenException("User account is disabled");
        }

        if (!user.isAccountNonLocked()) {
            log.warn("Locked user attempted refresh: userId={}", user.getId());
            throw new InvalidRefreshTokenException("User account is locked");
        }

        refreshToken.setUsed(true);
        refreshTokenRepository.save(refreshToken);

        String authorities = user.getRole().getAuthorities().stream()
                .map(a -> a.getName())
                .collect(Collectors.joining(","));

        String newAccessToken = tokenAuthenticationService.generateAccessToken(
                user.getId().toString(), user.getUsername(), authorities);
        String newRefreshToken = createRefreshToken(user.getId());

        log.info("Access token refreshed for userId={}", user.getId());
        return new RefreshResult(newAccessToken, newRefreshToken);
    }

    public record RefreshResult(String accessToken, String refreshToken) {
    }
}
