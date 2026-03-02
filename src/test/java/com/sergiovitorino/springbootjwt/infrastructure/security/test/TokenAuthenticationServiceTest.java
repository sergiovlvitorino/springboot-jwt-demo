package com.sergiovitorino.springbootjwt.infrastructure.security.test;

import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class TokenAuthenticationServiceTest {

    @Autowired
    private TokenAuthenticationService service;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    public void testIfAddAuthenticationThrowsIllegalArgumentExceptionWhenUsernameNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
            service.addAuthentication(mock(HttpServletResponse.class), "notfound@x.com")
        );
    }

    @Test
    public void testIfAddAuthenticationWritesValidJwtHeader() {
        var response = new MockHttpServletResponse();
        service.addAuthentication(response, "abc@def.com");

        String header = response.getHeader(TokenAuthenticationService.HEADER_STRING);
        assertNotNull(header);
        assertTrue(header.startsWith("Bearer "));

        String token = header.substring("Bearer ".length()).trim();
        var jwt = jwtDecoder.decode(token);
        assertNotNull(jwt.getSubject());
        assertNotNull(jwt.getClaimAsString("authorities"));
    }
}
