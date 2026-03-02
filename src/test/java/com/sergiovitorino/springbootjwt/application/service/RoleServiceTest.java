package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void findAll_success() {
        Page<Role> page = new PageImpl<>(Collections.emptyList());
        when(repository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);

        Page<Role> result = roleService.findAll(0, 10, "name", true, new Role());

        assertNotNull(result);
        verify(repository).findAll(any(Example.class), any(Pageable.class));
    }

    @Test
    void count_success() {
        when(repository.count(any(Example.class))).thenReturn(10L);

        Long result = roleService.count(new Role());

        assertEquals(10L, result);
        verify(repository).count(any(Example.class));
    }
}
