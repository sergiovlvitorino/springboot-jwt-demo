package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.exception.EmailAlreadyExistsException;
import com.sergiovitorino.springbootjwt.domain.exception.ResourceNotFoundException;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import com.sergiovitorino.springbootjwt.infrastructure.security.UserLogged;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserLogged userLogged;

    @InjectMocks
    private UserService service;

    @Test
    void loadUserByUsername_success() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(repository.findByEmail(email)).thenReturn(user);

        UserDetails result = service.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals(email, result.getUsername());
    }

    @Test
    void loadUserByUsername_notFound() {
        String email = "notfound@example.com";
        when(repository.findByEmail(email)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(email));
    }

    @Test
    void save_success() {
        User user = new User();
        user.setEmail("new@example.com");
        user.setPassword("rawPassword");

        when(repository.findByEmail(user.getEmail())).thenReturn(null);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userLogged.getUserId()).thenReturn(UUID.randomUUID());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User savedUser = service.save(user);

        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        assertNotNull(savedUser.getDateCreatedAt());
        assertNotNull(savedUser.getUserIdCreatedAt());
        assertTrue(savedUser.isEnabled());
    }

    @Test
    void save_emailAlreadyExists() {
        User user = new User();
        user.setEmail("existing@example.com");

        when(repository.findByEmail(user.getEmail())).thenReturn(new User());

        assertThrows(EmailAlreadyExistsException.class, () -> service.save(user));
        verify(repository, never()).save(any());
    }

    @Test
    void update_success() {
        UUID id = UUID.randomUUID();
        User inputUser = new User();
        inputUser.setId(id);
        inputUser.setName("New Name");

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setName("Old Name");

        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userLogged.getUserId()).thenReturn(UUID.randomUUID());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User updatedUser = service.update(inputUser);

        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        assertNotNull(updatedUser.getDateUpdatedAt());
        assertNotNull(updatedUser.getUserIdUpdatedAt());
    }

    @Test
    void update_notFound() {
        UUID id = UUID.randomUUID();
        User inputUser = new User();
        inputUser.setId(id);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(inputUser));
        verify(repository, never()).save(any());
    }

    @Test
    void disable_success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setEnabled(true);

        when(repository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userLogged.getUserId()).thenReturn(UUID.randomUUID());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User disabledUser = service.disable(id);

        assertNotNull(disabledUser);
        assertFalse(disabledUser.isEnabled());
        assertNotNull(disabledUser.getDateDisabledAt());
        assertNotNull(disabledUser.getUserIdDisabledAt());
    }

    @Test
    void disable_notFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.disable(id));
        verify(repository, never()).save(any());
    }
}
