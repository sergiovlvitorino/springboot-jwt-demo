package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleRepository repository;

    public Page<Role> findAll(Integer pageNumber, Integer pageSize, String orderBy, Boolean asc, Role role) {
        final var direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sort = Sort.by(direction, orderBy);
        final var pageable = PageRequest.of(pageNumber, pageSize, sort);
        final var matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final var example = Example.of(role, matcher);
        return repository.findAll(example, pageable);
    }

    public Long count(Role role) {
        final var matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final var example = Example.of(role, matcher);
        return repository.count(example);
    }

}
