package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimitFilterTest {

    private static final int TEST_MAX_ATTEMPTS = 10;
    private LoginRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LoginRateLimitFilter(TEST_MAX_ATTEMPTS);
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

        for (int i = 0; i < TEST_MAX_ATTEMPTS; i++) {
            assertFalse(filter.isRateLimited(ip), "Request " + i + " should not be rate limited");
        }

        assertTrue(filter.isRateLimited(ip), "Request after limit should be rate limited");
    }

    @Test
    void shouldReturn429WhenRateLimited() throws Exception {
        String ip = "10.0.0.2";

        for (int i = 0; i < TEST_MAX_ATTEMPTS; i++) {
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
        for (int i = 0; i < TEST_MAX_ATTEMPTS; i++) {
            filter.isRateLimited("ip1");
        }
        assertTrue(filter.isRateLimited("ip1"));
        assertFalse(filter.isRateLimited("ip2"));
    }

    @Test
    void shouldUseXForwardedForWhenPresent() throws Exception {
        // Comportamento atualizado: atrás de proxy, deve usar o IP do header XFF
        var request = createLoginRequest("192.168.1.100");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        // Rate limiting deve usar o IP do X-Forwarded-For, não o remoteAddr do proxy
        assertTrue(filter.getAttempts().containsKey("1.2.3.4"));
        assertFalse(filter.getAttempts().containsKey("192.168.1.100"));
    }

    @Test
    void extractClientIp_shouldReturnFirstIpFromXForwardedForChain() {
        // Quando XFF tem cadeia de proxies, deve usar o primeiro (IP real do cliente)
        var request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 10.0.0.1, 172.16.0.1");

        String clientIp = filter.extractClientIp(request);

        assertEquals("1.2.3.4", clientIp);
    }

    @Test
    void extractClientIp_shouldFallbackToRemoteAddrWhenXffAbsent() {
        // Sem header XFF, deve usar remoteAddr normalmente
        var request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setRemoteAddr("203.0.113.5");

        String clientIp = filter.extractClientIp(request);

        assertEquals("203.0.113.5", clientIp);
    }

    @Test
    void extractClientIp_shouldFallbackToRemoteAddrWhenXffIsBlank() {
        // Header XFF presente mas vazio deve ser ignorado
        var request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setRemoteAddr("203.0.113.5");
        request.addHeader("X-Forwarded-For", "   ");

        String clientIp = filter.extractClientIp(request);

        assertEquals("203.0.113.5", clientIp);
    }

    @Test
    void shouldRateLimitBasedOnXForwardedForIp() throws Exception {
        // Rate limiting deve ser aplicado corretamente ao IP extraído do XFF
        String realClientIp = "5.5.5.5";
        String proxyIp = "10.10.10.10";

        // Esgota as tentativas usando o IP real do XFF
        for (int i = 0; i < TEST_MAX_ATTEMPTS; i++) {
            filter.isRateLimited(realClientIp);
        }

        // Próximo request do mesmo cliente (via proxy) deve ser bloqueado
        var request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setRemoteAddr(proxyIp);
        request.addHeader("X-Forwarded-For", realClientIp);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("Too many login attempts"));
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
