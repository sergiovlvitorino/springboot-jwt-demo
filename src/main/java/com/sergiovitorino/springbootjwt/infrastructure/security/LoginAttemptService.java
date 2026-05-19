package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_MAP_SIZE = 10_000;

    @Value("${security.account-lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.account-lockout.window-millis:900000}")
    private long windowMillis;

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, AttemptRecord> attemptsMap = new ConcurrentHashMap<>();

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void loginFailed(String email) {
        if (email == null) return;
        String key = email.toLowerCase();

        if (attemptsMap.size() >= MAX_MAP_SIZE) {
            evictExpiredEntries();
        }

        AttemptRecord record = attemptsMap.compute(key, (k, existing) -> {
            if (existing == null) return new AttemptRecord(1);
            existing.increment();
            return existing;
        });

        if (record.count() == maxAttempts) {
            lockAccount(key);
        }
    }

    public void loginSucceeded(String email) {
        if (email == null) return;
        attemptsMap.remove(email.toLowerCase());
    }

    @Scheduled(fixedRate = 60_000)
    public void evictExpiredEntries() {
        Instant cutoff = Instant.now().minusMillis(windowMillis);
        attemptsMap.entrySet().removeIf(e -> e.getValue().lastUpdated().isBefore(cutoff));
        log.debug("Evicted expired login attempt entries; map size={}", attemptsMap.size());
    }

    private void lockAccount(String emailLower) {
        userRepository.findByEmail(emailLower).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getAccountLocked())) return;
            user.setAccountLocked(true);
            userRepository.save(user);
            attemptsMap.remove(emailLower);
            log.warn("Account locked: userId={}", user.getId());
        });
    }

    static final class AttemptRecord {

        private final AtomicInteger counter;
        private volatile Instant lastUpdated;

        AttemptRecord(int initialCount) {
            this.counter = new AtomicInteger(initialCount);
            this.lastUpdated = Instant.now();
        }

        void increment() {
            counter.incrementAndGet();
            lastUpdated = Instant.now();
        }

        int count() {
            return counter.get();
        }

        Instant lastUpdated() {
            return lastUpdated;
        }
    }
}
