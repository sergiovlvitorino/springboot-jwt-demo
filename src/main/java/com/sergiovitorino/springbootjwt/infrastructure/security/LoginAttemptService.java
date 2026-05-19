package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_MAP_SIZE = 10_000;

    @Value("${security.account-lockout.max-attempts:5}")
    private int maxAttempts;

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, Integer> attemptsMap = new ConcurrentHashMap<>();

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void loginFailed(String email) {
        if (email == null) return;
        String key = email.toLowerCase();

        if (attemptsMap.size() >= MAX_MAP_SIZE) {
            attemptsMap.clear();
        }

        int attempts = attemptsMap.merge(key, 1, Integer::sum);
        if (attempts >= maxAttempts) {
            lockAccount(key);
        }
    }

    public void loginSucceeded(String email) {
        if (email == null) return;
        attemptsMap.remove(email.toLowerCase());
    }

    private void lockAccount(String emailLower) {
        userRepository.findByEmail(emailLower).ifPresent(user -> {
            user.setAccountLocked(true);
            userRepository.save(user);
            attemptsMap.remove(emailLower);
            log.warn("Account locked: userId={}", user.getId());
        });
    }
}
