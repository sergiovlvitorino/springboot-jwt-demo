package com.sergiovitorino.springbootjwt.application.command.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SaveCommandTest {

    @Test
    void recordFunctionality() {
        UUID roleId = UUID.randomUUID();

        SaveCommand c1 = new SaveCommand("Sergio", "sergio@example.com", "secret123", roleId);
        SaveCommand c2 = new SaveCommand("Sergio", "sergio@example.com", "secret123", roleId);

        assertEquals("Sergio", c1.name());
        assertEquals("sergio@example.com", c1.email());
        assertEquals("secret123", c1.password());
        assertEquals(roleId, c1.roleId());

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertEquals(c1.toString(), c2.toString());

        SaveCommand c3 = new SaveCommand("Other", "sergio@example.com", "secret123", roleId);
        assertNotEquals(c1, c3);
    }
}
