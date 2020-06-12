package com.sergiovitorino.springbootjwt.infrastructure.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
