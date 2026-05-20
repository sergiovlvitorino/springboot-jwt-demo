package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsAdapterTest {

    @Test
    void getAuthorities_returnsGuestWhenRoleNull() {
        User user = new User();
        user.setRole(null);

        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        List<String> names = adapter.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_GUEST"), names);
    }

    @Test
    void getAuthorities_returnsGuestWhenRoleHasNoAuthorities() {
        User user = new User();
        user.setRole(new Role());

        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        List<String> names = adapter.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_GUEST"), names);
    }

    @Test
    void getAuthorities_returnsAuthoritiesFromRole() {
        Role role = new Role();
        role.setAuthorities(List.of(new Authority("ROLE_ADMIN"), new Authority("ROLE_USER")));

        User user = new User();
        user.setRole(role);

        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        List<String> names = adapter.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), names);
    }

    @Test
    void isEnabled_falseWhenEnabledNull() {
        User user = new User();
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        assertFalse(adapter.isEnabled());
    }

    @Test
    void isEnabled_trueWhenEnabled() {
        User user = new User();
        user.setEnabled(true);
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        assertTrue(adapter.isEnabled());
    }

    @Test
    void isAccountNonLocked_trueByDefault() {
        User user = new User();
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        assertTrue(adapter.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_falseWhenLocked() {
        User user = new User();
        user.setAccountLocked(true);
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        assertFalse(adapter.isAccountNonLocked());
    }

    @Test
    void isAccountNonExpired_alwaysTrue() {
        UserDetailsAdapter adapter = new UserDetailsAdapter(new User());
        assertTrue(adapter.isAccountNonExpired());
    }

    @Test
    void isCredentialsNonExpired_alwaysTrue() {
        UserDetailsAdapter adapter = new UserDetailsAdapter(new User());
        assertTrue(adapter.isCredentialsNonExpired());
    }

    @Test
    void getUsername_returnsEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        assertEquals("test@example.com", adapter.getUsername());
    }

    @Test
    void getPassword_returnsUserPassword() {
        User user = new User();
        user.setPassword("encodedPass");
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        assertEquals("encodedPass", adapter.getPassword());
    }

    @Test
    void getUser_returnsOriginalUser() {
        User user = new User();
        user.setEmail("user@example.com");
        UserDetailsAdapter adapter = new UserDetailsAdapter(user);
        assertSame(user, adapter.getUser());
    }
}
