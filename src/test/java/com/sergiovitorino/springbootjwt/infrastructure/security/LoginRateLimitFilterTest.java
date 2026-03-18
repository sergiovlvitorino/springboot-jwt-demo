package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimitFilterTest {

    private LoginRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LoginRateLimitFilter();
    }

    @Test
    void shouldAllowRequestsBelowLimit() throws Exception {
        var request = createLoginRequest("192.168.1.1");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertNotEquals(429, response.getStatus());
    }

    @Test
    void shouldBlockRequestsAboveLimit() throws Exception {
        String ip = "10.0.0.1";

        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS; i++) {
            assertFalse(filter.isRateLimited(ip), "Request " + i + " should not be rate limited");
        }

        assertTrue(filter.isRateLimited(ip), "Request after limit should be rate limited");
    }

    @Test
    void shouldReturn429WhenRateLimited() throws Exception {
        String ip = "10.0.0.2";

        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS; i++) {
            filter.isRateLimited(ip);
        }

        var request = createLoginRequest(ip);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("Too many login attempts"));
    }

    @Test
    void shouldNotRateLimitNonLoginRequests() throws Exception {
        var request = new MockHttpServletRequest("GET", "/rest/user");
        request.setRemoteAddr("10.0.0.3");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertNotEquals(429, response.getStatus());
    }

    @Test
    void shouldTrackDifferentIpsSeparately() {
        for (int i = 0; i < LoginRateLimitFilter.MAX_ATTEMPTS; i++) {
            filter.isRateLimited("ip1");
        }
        assertTrue(filter.isRateLimited("ip1"));
        assertFalse(filter.isRateLimited("ip2"));
    }

    @Test
    void shouldUseRemoteAddrNotXForwardedFor() throws Exception {
        var request = createLoginRequest("192.168.1.100");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        // Rate limit tracking should use remoteAddr, not X-Forwarded-For
        assertTrue(filter.getAttempts().containsKey("192.168.1.100"));
        assertFalse(filter.getAttempts().containsKey("1.2.3.4"));
    }

    @Test
    void shouldEvictExpiredEntries() {
        filter.isRateLimited("expired-ip");
        assertFalse(filter.getAttempts().isEmpty());

        // Manually clear timestamps to simulate expiration
        filter.getAttempts().get("expired-ip").clear();
        filter.evictExpiredEntries();

        assertFalse(filter.getAttempts().containsKey("expired-ip"));
    }

    private MockHttpServletRequest createLoginRequest(String remoteAddr) {
        var request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
