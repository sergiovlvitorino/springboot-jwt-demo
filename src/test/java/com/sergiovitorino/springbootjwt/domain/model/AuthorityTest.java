package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthorityTest {

    @Test
    void equalsAndHashCode_basedOnIdOnly() {
        UUID id = UUID.randomUUID();

        Authority a1 = new Authority();
        a1.setId(id);
        a1.setName("ROLE_ADMIN");

        Authority a2 = new Authority(id, "ROLE_USER", null);

        // Same ID → equal, regardless of name
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        // Different ID → not equal
        Authority a3 = new Authority();
        a3.setId(UUID.randomUUID());
        a3.setName("ROLE_ADMIN");
        assertNotEquals(a1, a3);

        // Identity and null checks
        assertEquals(a1, a1);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "x");
    }

    @Test
    void toString_format() {
        Authority a = new Authority();
        a.setId(UUID.randomUUID());
        a.setName("ROLE_ADMIN");

        String s = a.toString();
        assertTrue(s.contains("Authority{"));
        assertTrue(s.contains("name='ROLE_ADMIN'"));
    }
}
