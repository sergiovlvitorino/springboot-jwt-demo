package com.sergiovitorino.springbootjwt.infrastructure.security.test;

import com.sergiovitorino.springbootjwt.infrastructure.security.TokenAuthenticationService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

}
