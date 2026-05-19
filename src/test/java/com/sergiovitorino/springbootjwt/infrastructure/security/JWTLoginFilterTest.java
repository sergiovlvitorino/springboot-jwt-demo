package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JWTLoginFilterTest {

    @Test
    void shouldMaskEmailCorrectly() {
        assertEquals("a***@def.com", PiiMasker.maskEmail("abc@def.com"));
        assertEquals("s***@example.com", PiiMasker.maskEmail("sergio@example.com"));
    }

    @Test
    void shouldMaskShortEmail() {
        assertEquals("*@x.com", PiiMasker.maskEmail("a@x.com"));
    }

    @Test
    void shouldMaskNullEmail() {
        assertEquals("***", PiiMasker.maskEmail(null));
    }

    @Test
    void shouldMaskEmailWithoutAtSign() {
        assertEquals("***", PiiMasker.maskEmail("noemail"));
    }
}
