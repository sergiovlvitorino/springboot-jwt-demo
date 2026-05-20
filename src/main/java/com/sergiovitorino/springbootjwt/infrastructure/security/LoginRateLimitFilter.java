package com.sergiovitorino.springbootjwt.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginRateLimitFilter extends OncePerRequestFilter {

    static final long WINDOW_MILLIS = 60_000L;
    private static final int MAX_CACHE_SIZE = 10_000;

    private final int maxAttempts;
    private final boolean trustedProxyEnabled;
    private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(
            @Value("${login.rate-limit.max-attempts:10}") int maxAttempts,
            @Value("${security.trusted-proxy.enabled:false}") boolean trustedProxyEnabled) {
        this.maxAttempts = maxAttempts;
        this.trustedProxyEnabled = trustedProxyEnabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())
                && ("/login".equals(request.getServletPath()) || "/auth/refresh".equals(request.getServletPath()))) {
            String ip = extractClientIp(request);
            if (isRateLimited(ip)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many login attempts. Please try again later.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    String extractClientIp(HttpServletRequest request) {
        if (trustedProxyEnabled) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                String firstIp = xff.split(",")[0].trim();
                if (!firstIp.isEmpty()) {
                    return firstIp;
                }
            }
        }
        return request.getRemoteAddr();
    }

    boolean isRateLimited(String ip) {
        if (attempts.size() >= MAX_CACHE_SIZE) {
            evictExpiredEntries();
        }
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = attempts.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxAttempts) {
                return true;
            }
            timestamps.addLast(now);
            return false;
        }
    }

    @Scheduled(fixedRate = 60_000)
    void evictExpiredEntries() {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                    timestamps.pollFirst();
                }
                return timestamps.isEmpty();
            }
        });
    }

    // Visible for testing
    ConcurrentHashMap<String, Deque<Long>> getAttempts() {
        return attempts;
    }
}
