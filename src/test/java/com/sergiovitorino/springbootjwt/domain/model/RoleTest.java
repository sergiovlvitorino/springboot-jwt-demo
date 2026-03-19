package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void getAuthorities_initializesListWhenNull() {
        Role role = new Role();
        assertNotNull(role.getAuthorities());
        assertTrue(role.getAuthorities().isEmpty());

        role.getAuthorities().add(new Authority("ROLE_USER"));
        assertEquals(1, role.getAuthorities().size());
    }

    @Test
    void equalsAndHashCode_basedOnIdOnly() {
        UUID id = UUID.randomUUID();

        Role r1 = new Role(id, "ADMIN", null, List.of(new Authority("ROLE_ADMIN")));
        Role r2 = new Role(id, "DIFFERENT", null, List.of());

        // Same ID → equal
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        // Different ID → not equal
        Role r3 = new Role(UUID.randomUUID(), "ADMIN", null, List.of(new Authority("ROLE_ADMIN")));
        assertNotEquals(r1, r3);

        // Identity and null checks
        assertEquals(r1, r1);
        assertNotEquals(r1, null);
        assertNotEquals(r1, "x");
    }

    @Test
    void toString_doesNotIncludeCollections() {
        Role r = new Role(UUID.randomUUID(), "ADMIN", null, null);
        String s = r.toString();
        assertTrue(s.contains("Role{"));
        assertTrue(s.contains("name='ADMIN'"));
        assertFalse(s.contains("users"));
    }
}
