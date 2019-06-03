package com.sergiovitorino.springbootjwt.infrastructure.security.test;

import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TokenAuthenticationServiceTest {

    private TokenAuthenticationService service;

    @Before
    public void setUp(){
        service = new TokenAuthenticationService();
    }

    @Test
    public void testIfRemoveCommaIsOk(){
        String expectedText = "";
        String actualText = service.removeComma("");
        assertEquals(expectedText, actualText);
    }

    @Test
    public void testIfGetAuthenticationReturnsNullWhenUserIdIsNull(){
        String token = Jwts.builder()
                .setSubject(null)
                .setExpiration(new Date(System.currentTimeMillis() + TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        Authentication authentication = this.service.getAuthentication(request);
        assertNull(authentication);
    }

    @Test
    public void testIfGetAuthenticationReturnsNullWhenUserIdIsEmpty(){
        String token = Jwts.builder()
                .setSubject("")
                .setExpiration(new Date(System.currentTimeMillis() + TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        Authentication authentication = this.service.getAuthentication(request);
        assertNull(authentication);
    }

    @Test
    public void testIfGetAuthenticationReturnsNullWhenExpired(){
        String token = Jwts.builder()
                .setSubject("")
                .setExpiration(new Date(System.currentTimeMillis() - TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        Authentication authentication = this.service.getAuthentication(request);
        assertNull(authentication);
    }

}
