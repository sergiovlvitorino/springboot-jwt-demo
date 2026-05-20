package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    private static final int MAX_ATTEMPTS = 3;

    @Mock
    private UserRepository userRepository;

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(userRepository);
        ReflectionTestUtils.setField(service, "maxAttempts", MAX_ATTEMPTS);
        ReflectionTestUtils.setField(service, "windowMillis", 900_000L);
    }

    @Test
    void loginFailed_shouldIncrementAttemptsCounter() {
        service.loginFailed("user@example.com");
        service.loginFailed("user@example.com");

        // 2 failed attempts — not yet at threshold, no lock
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void loginFailed_shouldLockAccountWhenThresholdReached() {
        User user = buildUser("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.loginFailed("user@example.com");
        }

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountLocked()).isTrue();
    }

    @Test
    void loginFailed_shouldNormalizeCaseBeforeTracking() {
        User user = buildUser("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.loginFailed("USER@EXAMPLE.COM");
        service.loginFailed("User@Example.Com");
        service.loginFailed("user@example.com");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginSucceeded_shouldResetCounter() {
        // Accumulate 2 attempts
        service.loginFailed("reset@example.com");
        service.loginFailed("reset@example.com");

        // Successful login resets the counter
        service.loginSucceeded("reset@example.com");

        // Now 2 more failures should NOT lock (counter was reset)
        service.loginFailed("reset@example.com");
        service.loginFailed("reset@example.com");

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void loginFailed_shouldLockExactlyOnceWhenThresholdHit() {
        User user = buildUser("idempotent@example.com");
        when(userRepository.findByEmail("idempotent@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Hit threshold exactly — only one save
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.loginFailed("idempotent@example.com");
        }

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void loginFailed_beyondThreshold_doesNotTriggerLockAgain() {
        User user = buildUser("beyond@example.com");
        when(userRepository.findByEmail("beyond@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Exactly at threshold — locks once and removes from map
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.loginFailed("beyond@example.com");
        }

        // After lock, the entry is removed; further calls restart the counter from 1
        // and will not trigger lock until MAX_ATTEMPTS failures again
        service.loginFailed("beyond@example.com");

        // Still only one lock
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void lockAccount_isIdempotent_doesNotSaveAlreadyLockedUser() {
        User alreadyLocked = buildUser("locked@example.com");
        alreadyLocked.setAccountLocked(true);
        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(alreadyLocked));

        // Simulate threshold exactly while account is already locked
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.loginFailed("locked@example.com");
        }

        // lockAccount should short-circuit and not call save
        verify(userRepository, never()).save(any());
    }

    @Test
    void evictExpiredEntries_removesStaleRecords() {
        // Inject a very short window so entries expire immediately
        ReflectionTestUtils.setField(service, "windowMillis", 1L);

        service.loginFailed("stale@example.com");

        // Small sleep to ensure lastUpdated is before cutoff
        try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        service.evictExpiredEntries();

        // After eviction the entry is gone; failing again starts count from 1 — no lock
        ReflectionTestUtils.setField(service, "windowMillis", 900_000L);
        service.loginFailed("stale@example.com");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void evictExpiredEntries_keepsRecentRecords() {
        ReflectionTestUtils.setField(service, "windowMillis", 900_000L);

        service.loginFailed("recent@example.com");
        service.loginFailed("recent@example.com");

        service.evictExpiredEntries();

        // Entry still alive — one more failure reaches threshold
        User user = buildUser("recent@example.com");
        when(userRepository.findByEmail("recent@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.loginFailed("recent@example.com");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginFailed_shouldIgnoreNullEmail() {
        service.loginFailed(null);
        verifyNoInteractions(userRepository);
    }

    @Test
    void loginSucceeded_shouldIgnoreNullEmail() {
        service.loginSucceeded(null);
        verifyNoInteractions(userRepository);
    }

    @Test
    void loginFailed_shouldDoNothingWhenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.loginFailed("ghost@example.com");
        }

        verify(userRepository, never()).save(any());
    }

    private User buildUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setAccountLocked(false);
        return user;
    }
}
