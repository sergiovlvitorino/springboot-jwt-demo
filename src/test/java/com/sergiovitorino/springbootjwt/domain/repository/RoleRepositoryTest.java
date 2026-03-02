package com.sergiovitorino.springbootjwt.domain.repository;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findById_returnsRole_whenExists() {
        Role role = new Role();
        role.setName("ROLE_TEST");
        role.setAuthorities(Collections.emptyList());
        Role persisted = entityManager.persistAndFlush(role);

        Optional<Role> found = roleRepository.findById(persisted.getId());

        assertTrue(found.isPresent());
        assertEquals("ROLE_TEST", found.get().getName());
    }

    @Test
    void save_persistsRole() {
        Role role = new Role();
        role.setName("ROLE_NEW");
        role.setAuthorities(Collections.emptyList());

        Role saved = roleRepository.save(role);
        entityManager.flush();

        assertNotNull(saved.getId());
        Role found = entityManager.find(Role.class, saved.getId());
        assertNotNull(found);
        assertEquals("ROLE_NEW", found.getName());
    }
}
