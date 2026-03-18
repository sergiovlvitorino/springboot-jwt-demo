package com.sergiovitorino.springbootjwt.application.command.user;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseTest {

    @Test
    void shouldMapUserToResponse() {
        var role = new Role();
        role.setName("ADMIN");

        var user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("test-only-value");
        user.setEnabled(true);
        user.setRole(role);

        var response = UserResponse.from(user);

        assertEquals(user.getId(), response.id());
        assertEquals("John", response.name());
        assertEquals("john@example.com", response.email());
        assertTrue(response.enabled());
        assertEquals("ADMIN", response.roleName());
        assertNull(response.dateCreatedAt());
    }

    @Test
    void shouldHandleNullRole() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Jane");
        user.setEmail("jane@example.com");
        user.setEnabled(true);

        var response = UserResponse.from(user);

        assertNull(response.roleName());
    }

    @Test
    void shouldNotExposePassword() throws Exception {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test");
        user.setEmail("test@test.com");
        var testPassword = "Test@" + UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(testPassword);
        user.setEnabled(true);

        var response = UserResponse.from(user);

        // UserResponse record has no password field
        var json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(response);
        assertFalse(json.contains(testPassword), "Password must not appear in serialized response");
        assertFalse(json.contains("password"), "Password field must not exist in response");
    }
}
