package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    void equalsHashCodeAndToString_coverCommonBranches() {
        UUID id = UUID.randomUUID();
        List<User> users = new ArrayList<>();
        List<Authority> authorities = new ArrayList<>(List.of(new Authority("ROLE_ADMIN")));

        Role r1 = new Role(id, "ADMIN", users, authorities);
        Role r2 = new Role(id, "ADMIN", new ArrayList<>(users), new ArrayList<>(authorities));

        assertEquals(r1, r1);
        assertNotEquals(r1, null);
        assertNotEquals(r1, "x");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        Role r3 = new Role(id, "DIFFERENT", users, authorities);
        assertNotEquals(r1, r3);

        String s = r1.toString();
        assertTrue(s.contains("Role{"));
        assertTrue(s.contains("name='ADMIN'"));
    }
}


