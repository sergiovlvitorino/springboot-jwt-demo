package com.sergiovitorino.springbootjwt.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getEnabled_defaultsToNullWhenNotSet() {
        User user = new User();
        assertNull(user.getEnabled());
    }

    @Test
    void getEnabled_trueWhenSet() {
        User user = new User();
        user.setEnabled(true);
        assertTrue(user.getEnabled());
    }

    @Test
    void getAccountLocked_defaultsToFalse() {
        User user = new User();
        assertFalse(Boolean.TRUE.equals(user.getAccountLocked()));
    }

    @Test
    void getAccountLocked_trueWhenSet() {
        User user = new User();
        user.setAccountLocked(true);
        assertTrue(user.getAccountLocked());
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
