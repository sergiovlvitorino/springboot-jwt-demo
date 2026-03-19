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
        assertFalse(user.isEnabled());
    }

    @Test
    void isAccountNonLocked_defaultsToTrue() {
        User user = new User();
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_falseWhenLocked() {
        User user = new User();
        user.setAccountLocked(true);
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void equalsAndHashCode_basedOnIdOnly() {
        UUID id = UUID.randomUUID();

        User u1 = new User(id, "Sergio", "sergio@example.com", "pass1", true, new Role());
        User u2 = new User(id, "Other", "other@example.com", "pass2", false, null);

        // Same ID → equal, regardless of other fields
        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());

        // Different ID → not equal
        User u3 = new User(UUID.randomUUID(), "Sergio", "sergio@example.com", "pass1", true, new Role());
        assertNotEquals(u1, u3);

        // Identity and null checks
        assertEquals(u1, u1);
        assertNotEquals(u1, null);
        assertNotEquals(u1, "x");
    }

    @Test
    void toString_protectsPassword() {
        User u = new User(UUID.randomUUID(), "Sergio", "sergio@example.com", "mysecret", true, null);
        String s = u.toString();
        assertTrue(s.contains("User{"));
        assertTrue(s.contains("email='sergio@example.com'"));
        assertTrue(s.contains("[PROTECTED]"));
        assertFalse(s.contains("mysecret"));
    }
}
