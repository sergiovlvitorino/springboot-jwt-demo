package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JWTLoginFilterTest {

    @Test
    void shouldMaskEmailCorrectly() {
        assertEquals("a***@def.com", JWTLoginFilter.maskEmail("abc@def.com"));
        assertEquals("s***@example.com", JWTLoginFilter.maskEmail("sergio@example.com"));
    }

    @Test
    void shouldMaskShortEmail() {
        assertEquals("*@x.com", JWTLoginFilter.maskEmail("a@x.com"));
    }

    @Test
    void shouldMaskNullEmail() {
        assertEquals("***", JWTLoginFilter.maskEmail(null));
    }

    @Test
    void shouldMaskEmailWithoutAtSign() {
        assertEquals("***", JWTLoginFilter.maskEmail("noemail"));
    }
}
