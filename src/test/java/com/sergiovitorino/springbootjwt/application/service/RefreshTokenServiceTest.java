package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.exception.InvalidRefreshTokenException;
import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.model.RefreshToken;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.RefreshTokenRepository;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
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
    void createRefreshToken_savesAndReturnsToken() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        String token = refreshTokenService.createRefreshToken(userId);

        assertThat(token).isNotNull().isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshAccessToken_success() {
        UUID userId = UUID.randomUUID();
        String tokenValue = UUID.randomUUID().toString();

        Authority authority = new Authority("ROLE_USER");
        Role role = new Role("USER", List.of(authority));
        User user = new User(userId, "Test User", "test@test.com", "pw", true, role);

        RefreshToken refreshToken = new RefreshToken(tokenValue, userId, LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByIdWithAuthorities(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenAuthenticationService.generateAccessToken(any(), any(), any())).thenReturn("new-access-token");

        RefreshTokenService.RefreshResult result = refreshTokenService.refreshAccessToken(tokenValue);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isNotNull();
        assertThat(refreshToken.isUsed()).isTrue();
    }

    @Test
    void refreshAccessToken_tokenNotFound_throwsException() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("unknown"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshAccessToken_tokenAlreadyUsed_throwsException() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken usedToken = new RefreshToken(tokenValue, UUID.randomUUID(), LocalDateTime.now().plusDays(7));
        usedToken.setUsed(true);

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(tokenValue))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void refreshAccessToken_tokenExpired_throwsException() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken expiredToken = new RefreshToken(tokenValue, UUID.randomUUID(), LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(tokenValue))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void refreshAccessToken_userDisabled_throwsException() {
        UUID userId = UUID.randomUUID();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(tokenValue, userId, LocalDateTime.now().plusDays(7));

        User disabledUser = new User(userId, "Test", "test@test.com", "pw", false, new Role());

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByIdWithAuthorities(userId)).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(tokenValue))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void refreshAccessToken_userLocked_throwsException() {
        UUID userId = UUID.randomUUID();
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(tokenValue, userId, LocalDateTime.now().plusDays(7));

        User lockedUser = new User(userId, "Test", "test@test.com", "pw", true, new Role());
        lockedUser.setAccountLocked(true);

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(userRepository.findByIdWithAuthorities(userId)).thenReturn(Optional.of(lockedUser));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(tokenValue))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("locked");
    }
}
