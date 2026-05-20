package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtConfigTest {

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void shouldCreateJwtDecoderBean() {
        assertNotNull(jwtDecoder);
    }

    @Test
    void shouldCreateJwtEncoderBean() {
        assertNotNull(jwtEncoder);
    }

    @Test
    void shouldCreateJwtAuthenticationConverterBean() {
        assertNotNull(jwtAuthenticationConverter);
    }

    // --- validateSecret tests ---

    @Test
    void validateSecret_shouldThrowWhenSecretIsNull() {
        JwtConfig config = new JwtConfig();
        ReflectionTestUtils.setField(config, "secret", null);
        assertThrows(IllegalStateException.class, config::validateSecret);
    }

    @Test
    void validateSecret_shouldThrowWhenSecretIsTooShort() {
        JwtConfig config = new JwtConfig();
        ReflectionTestUtils.setField(config, "secret", "short");
        assertThrows(IllegalStateException.class, config::validateSecret);
    }

    @Test
    void validateSecret_shouldPassWith32Chars() {
        JwtConfig config = new JwtConfig();
        // Exatamente 32 chars — deve passar sem excecao
        ReflectionTestUtils.setField(config, "secret", "12345678901234567890123456789012");
        assertDoesNotThrow(config::validateSecret);
    }

    // --- jwtAuthenticationConverter tests ---

    /**
     * Constroi um Jwt de teste via builder, com ou sem claim authorities.
     * Usa o bean injetado pelo Spring para converter e verificar authorities resultantes.
     */
    private AbstractAuthenticationToken convertJwt(String authoritiesClaimValue) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("test-uuid")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("Username", "user@test.com");

        if (authoritiesClaimValue != null) {
            builder.claim("authorities", authoritiesClaimValue);
        }

        return jwtAuthenticationConverter.convert(builder.build());
    }

    @Test
    void jwtAuthenticationConverter_shouldHandleNullAuthoritiesClaim() {
        // Jwt sem claim "authorities" → authorities vazia
        var token = convertJwt(null);
        assertNotNull(token);
        assertTrue(token.getAuthorities().isEmpty());
    }

    @Test
    void jwtAuthenticationConverter_shouldHandleBlankAuthoritiesClaim() {
        var token = convertJwt("   ");
        assertNotNull(token);
        assertTrue(token.getAuthorities().isEmpty());
    }

    @Test
    void jwtAuthenticationConverter_shouldHandleSingleAuthority() {
        var token = convertJwt("USER_RETRIEVE");
        assertNotNull(token);
        assertEquals(1, token.getAuthorities().size());
        assertTrue(token.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("USER_RETRIEVE")));
    }

    @Test
    void jwtAuthenticationConverter_shouldHandleMultipleAuthorities() {
        var token = convertJwt("USER_RETRIEVE,USER_SAVE,ROLE_RETRIEVE");
        assertNotNull(token);
        assertEquals(3, token.getAuthorities().size());
    }

    @Test
    void jwtAuthenticationConverter_shouldTrimWhitespace() {
        var token = convertJwt(" A , B ");
        assertNotNull(token);
        assertEquals(2, token.getAuthorities().size());
        assertTrue(token.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("A")));
        assertTrue(token.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("B")));
    }
}
