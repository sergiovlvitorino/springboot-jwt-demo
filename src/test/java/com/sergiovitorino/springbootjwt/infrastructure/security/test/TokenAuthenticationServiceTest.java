package com.sergiovitorino.springbootjwt.infrastructure.security.test;

import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TokenAuthenticationServiceTest {

    @Autowired
    private TokenAuthenticationService service;

    @Test
    public void testIfRemoveCommaIsOk() {
        var expectedText = "";
        var actualText = service.removeComma("");
        assertEquals(expectedText, actualText);
    }

    @Test
    public void testIfGetAuthenticationReturnsNullWhenUserIdIsNull() {
        var token = Jwts.builder()
                .setSubject(null)
                .setExpiration(new Date(System.currentTimeMillis() + TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        var request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        var authentication = this.service.getAuthentication(request);
        assertNull(authentication);
    }

    @Test
    public void testIfGetAuthenticationReturnsNullWhenUserIdIsEmpty() {
        var token = Jwts.builder()
                .setSubject("")
                .setExpiration(new Date(System.currentTimeMillis() + TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        var request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        var authentication = this.service.getAuthentication(request);
        assertNull(authentication);
    }

    @Test
    public void testIfGetAuthenticationReturnsNullWhenExpired() {
        var token = Jwts.builder()
                .setSubject(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() - TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        var request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        var authentication = this.service.getAuthentication(request);
        assertNull(authentication);
    }

    @Test
    public void testIfGetAuthenticationReturnsAnAuthorizationObjectWhenAuthoritesAreEmpty() {
        var token = Jwts.builder()
                .setSubject(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() + TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        var request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        var authentication = this.service.getAuthentication(request);
        assertNotNull(authentication);
    }

    @Test
    public void testIfAddAuthenticationThrowsIllegalArgumentExceptionWhenUsernameNotFound() {
        try {
            service.addAuthentication(null, UUID.randomUUID().toString() + "@def.com");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

}
