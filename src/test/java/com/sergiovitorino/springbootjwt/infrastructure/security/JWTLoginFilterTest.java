package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTLoginFilterTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldMaskEmailCorrectly() {
        assertEquals("a***@def.com", PiiMasker.maskEmail("abc@def.com"));
        assertEquals("s***@example.com", PiiMasker.maskEmail("sergio@example.com"));
    }

    @Test
    void shouldMaskShortEmail() {
        assertEquals("*@x.com", PiiMasker.maskEmail("a@x.com"));
    }

    @Test
    void shouldMaskNullEmail() {
        assertEquals("***", PiiMasker.maskEmail(null));
    }

    @Test
    void shouldMaskEmailWithoutAtSign() {
        assertEquals("***", PiiMasker.maskEmail("noemail"));
    }

    @Test
    void loginFailed_shouldLockAccountAfterNFailures() {
        LoginAttemptService service = new LoginAttemptService(userRepository);
        ReflectionTestUtils.setField(service, "maxAttempts", 3);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("victim@example.com");
        user.setAccountLocked(false);

        when(userRepository.findByEmail("victim@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.loginFailed("victim@example.com");
        service.loginFailed("victim@example.com");
        service.loginFailed("victim@example.com");

        verify(userRepository).save(argThat(u -> Boolean.TRUE.equals(u.getAccountLocked())));
    }

    @Test
    void loginSucceeded_shouldResetCounterSoNoLockOccurs() {
        LoginAttemptService service = new LoginAttemptService(userRepository);
        ReflectionTestUtils.setField(service, "maxAttempts", 3);

        service.loginFailed("ok@example.com");
        service.loginFailed("ok@example.com");
        service.loginSucceeded("ok@example.com");
        service.loginFailed("ok@example.com");
        service.loginFailed("ok@example.com");

        verify(userRepository, never()).save(any());
    }
}
