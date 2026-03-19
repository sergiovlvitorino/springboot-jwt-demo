package com.sergiovitorino.springbootjwt.domain.repository;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_returnsUser_whenExists() {
        // Arrange
        Role role = new Role();
        role.setName("ROLE_USER");
        entityManager.persist(role);

        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("secret");
        user.setRole(role);
        user.setEnabled(true);
        user.setDateCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(user);

        // Act
        var found = userRepository.findByEmail("john@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_returnsEmpty_whenNotExists() {
        var found = userRepository.findByEmail("nonexistent@example.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void save_persistsUserAndGeneratesId() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        entityManager.persist(role);

        User user = new User();
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPassword("secret");
        user.setRole(role);
        user.setEnabled(true);
        user.setDateCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        User found = entityManager.find(User.class, saved.getId());
        assertNotNull(found);
    }
}
