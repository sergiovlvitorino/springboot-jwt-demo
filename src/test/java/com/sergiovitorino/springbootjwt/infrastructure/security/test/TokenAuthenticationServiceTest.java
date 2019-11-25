package com.sergiovitorino.springbootjwt.infrastructure.security.test;

import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TokenAuthenticationServiceTest {

    @Autowired private TokenAuthenticationService service;

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
                .setSubject(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() - TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        Authentication authentication = this.service.getAuthentication(request);
        assertNull(authentication);
    }

    @Test
    public void testIfGetAuthenticationReturnsAnAuthorizationObjectWhenAuthoritesAreEmpty(){
        String token = Jwts.builder()
                .setSubject(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() + TokenAuthenticationService.EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, TokenAuthenticationService.SECRET).compact();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(TokenAuthenticationService.HEADER_STRING)).thenReturn(token);
        Authentication authentication = this.service.getAuthentication(request);
        assertNotNull(authentication);
    }

}
