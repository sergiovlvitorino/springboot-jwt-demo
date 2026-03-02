package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthorityTest {

    @Test
    void gettersSettersEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        List<Role> roles = new ArrayList<>();

        Authority a1 = new Authority();
        a1.setId(id);
        a1.setName("ROLE_ADMIN");
        a1.setRoles(roles);

        Authority a2 = new Authority(id, "ROLE_ADMIN", new ArrayList<>(roles));

        assertEquals(a1, a1);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "x");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        a2.setName("ROLE_USER");
        assertNotEquals(a1, a2);

        String s = a1.toString();
        assertTrue(s.contains("Authority{"));
        assertTrue(s.contains("name='ROLE_ADMIN'"));
    }
}


