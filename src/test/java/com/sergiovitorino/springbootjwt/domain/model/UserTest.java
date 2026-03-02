package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getAuthorities_returnsGuestWhenRoleNull() {
        User user = new User();
        user.setRole(null);

        List<String> names = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_GUEST"), names);
    }

    @Test
    void getAuthorities_returnsGuestWhenRoleHasNoAuthorities() {
        User user = new User();
        user.setRole(new Role());

        List<String> names = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_GUEST"), names);
    }

    @Test
    void getAuthorities_returnsAuthoritiesFromRole() {
        Role role = new Role();
        role.setAuthorities(List.of(new Authority("ROLE_ADMIN"), new Authority("ROLE_USER")));

        User user = new User();
        user.setRole(role);

        List<String> names = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), names);
    }

    @Test
    void isEnabled_defaultsToFalseWhenNull() {
        User user = new User();
        assertFalse(user.isEnabled());
        assertFalse(user.isEnabled()); // stable across calls
    }

    @Test
    void equalsHashCodeAndToString_coverCommonBranches() {
        UUID id = UUID.randomUUID();
        Role role = new Role(UUID.randomUUID());

        User u1 = new User(id, "Sergio", "sergio@example.com", "secret123", true, role);
        User u2 = new User(id, "Sergio", "sergio@example.com", "secret123", true, role);

        assertEquals(u1, u1);
        assertNotEquals(u1, null);
        assertNotEquals(u1, "x");

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());

        u2.setEmail("other@example.com");
        assertNotEquals(u1, u2);

        String s = u1.toString();
        assertTrue(s.contains("User{"));
        assertTrue(s.contains("email='sergio@example.com'"));
        assertTrue(s.contains("[PROTECTED]"));
        assertFalse(s.contains("secret123"));
    }
}


