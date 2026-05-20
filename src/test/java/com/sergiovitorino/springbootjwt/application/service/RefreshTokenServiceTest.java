package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.exception.InvalidRefreshTokenException;
import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.model.RefreshToken;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RefreshTokenRepository;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import com.sergiovitorino.springbootjwt.infrastructure.security.RefreshTokenHasher;
import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenAuthenticationService tokenAuthenticationService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);
    }

    @Test
    void createRefreshToken_savesAndReturnsRawToken() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        String rawToken = refreshTokenService.createRefreshToken(userId);

        assertThat(rawToken).isNotNull().isNotBlank();
        // raw token must NOT be the hash (hash is 64 hex chars)
        assertThat(rawToken).hasSizeLessThan(64).doesNotMatch("[0-9a-f]{64}");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshAccessToken_success() {
        UUID userId = UUID.randomUUID();
        String rawToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawToken);

        Authority authority = new Authority("ROLE_USER");
        Role role = new Role("USER", List.of(authority));
        User user = new User(userId, "Test User", "test@test.com", "pw", true, role);

        RefreshToken refreshToken = new RefreshToken(tokenHash, userId, LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByIdWithAuthorities(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenAuthenticationService.generateAccessToken(any(), any(), any())).thenReturn("new-access-token");

        RefreshTokenService.RefreshResult result = refreshTokenService.refreshAccessToken(rawToken);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isNotNull();
        assertThat(refreshToken.isUsed()).isTrue();
        assertThat(refreshToken.getUsedAt()).isNotNull();
    }

    @Test
    void refreshAccessToken_success_revokesParallelTokensAfterRotation() {
        UUID userId = UUID.randomUUID();
        String rawToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawToken);

        Authority authority = new Authority("ROLE_USER");
        Role role = new Role("USER", List.of(authority));
        User user = new User(userId, "Test User", "test@test.com", "pw", true, role);

        RefreshToken refreshToken = new RefreshToken(tokenHash, userId, LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByIdWithAuthorities(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenAuthenticationService.generateAccessToken(any(), any(), any())).thenReturn("new-access-token");
        when(refreshTokenRepository.revokeAllActiveByUserId(eq(userId), any(LocalDateTime.class))).thenReturn(1);

        refreshTokenService.refreshAccessToken(rawToken);

        // must revoke parallel tokens in the valid rotation path too
        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void refreshAccessToken_tokenNotFound_throwsException() {
        String rawToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawToken);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshAccessToken_tokenAlreadyUsed_revokesAllAndThrows() {
        UUID userId = UUID.randomUUID();
        String rawToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawToken);
        RefreshToken usedToken = new RefreshToken(tokenHash, userId, LocalDateTime.now().plusDays(7));
        usedToken.setUsed(true);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(usedToken));
        when(refreshTokenRepository.revokeAllActiveByUserId(eq(userId), any(LocalDateTime.class))).thenReturn(0);

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("already been used");

        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void refreshAccessToken_tokenExpired_throwsException() {
        String rawToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawToken);
        RefreshToken expiredToken = new RefreshToken(tokenHash, UUID.randomUUID(), LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void refreshAccessToken_userDisabled_throwsException() {
        UUID userId = UUID.randomUUID();
        String rawToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawToken);
        RefreshToken refreshToken = new RefreshToken(tokenHash, userId, LocalDateTime.now().plusDays(7));

        User disabledUser = new User(userId, "Test", "test@test.com", "pw", false, new Role());

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByIdWithAuthorities(userId)).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void refreshAccessToken_userLocked_throwsException() {
        UUID userId = UUID.randomUUID();
        String rawToken = RefreshTokenHasher.generateToken();
        String tokenHash = RefreshTokenHasher.hash(rawToken);
        RefreshToken refreshToken = new RefreshToken(tokenHash, userId, LocalDateTime.now().plusDays(7));

        User lockedUser = new User(userId, "Test", "test@test.com", "pw", true, new Role());
        lockedUser.setAccountLocked(true);

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByIdWithAuthorities(userId)).thenReturn(Optional.of(lockedUser));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("locked");
    }
}
