package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
