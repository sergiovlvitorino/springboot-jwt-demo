package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserLoggedTest {

    private UserLogged userLogged;

    @BeforeEach
    void setUp() {
        userLogged = new UserLogged();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserId_authenticated() {
        UUID expectedId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(expectedId.toString());
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID actualId = userLogged.getUserId();

        assertEquals(expectedId, actualId);
    }

    @Test
    void getUserId_notAuthenticated() {
        SecurityContextHolder.clearContext();

        UUID actualId = userLogged.getUserId();

        assertNull(actualId);
    }

    @Test
    void getUserId_invalidPrincipal() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("invalid-uuid");
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID actualId = userLogged.getUserId();

        assertNull(actualId);
    }
}
