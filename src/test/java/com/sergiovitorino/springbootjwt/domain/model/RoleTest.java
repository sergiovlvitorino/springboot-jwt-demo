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

    @Test
    void constructor_withAllArgs_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        Authority auth = new Authority("READ");
        User user = new User();
        List<Authority> authorities = List.of(auth);
        List<User> users = List.of(user);

        Role role = new Role(id, "ADMIN", users, authorities);

        assertEquals(id, role.getId());
        assertEquals("ADMIN", role.getName());
        assertEquals(users, role.getUsers());
        assertEquals(authorities, role.getAuthorities());
    }

    @Test
    void constructor_withNameAndAuthorities_shouldSetFields() {
        Authority auth = new Authority("WRITE");
        List<Authority> authorities = List.of(auth);

        Role role = new Role("EDITOR", authorities);

        assertEquals("EDITOR", role.getName());
        assertEquals(authorities, role.getAuthorities());
        assertNull(role.getId());
    }

    @Test
    void setUsers_shouldUpdateUsersList() {
        Role role = new Role();
        User user = new User();
        List<User> users = List.of(user);

        role.setUsers(users);

        assertEquals(users, role.getUsers());
    }

    @Test
    void getUsers_shouldReturnSetValue() {
        User user = new User();
        UUID id = UUID.randomUUID();
        Role role = new Role(id, "ROLE", List.of(user), List.of());

        assertNotNull(role.getUsers());
        assertEquals(1, role.getUsers().size());
    }
}
