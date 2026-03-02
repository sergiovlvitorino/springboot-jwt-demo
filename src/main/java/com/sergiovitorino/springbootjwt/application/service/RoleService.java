package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository repository;

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    public Page<Role> findAll(Integer pageNumber, Integer pageSize, String orderBy, Boolean asc, Role role) {
        log.debug("Fetching roles: page={}, size={}, orderBy={}, asc={}", pageNumber, pageSize, orderBy, asc);
        final var direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sort = Sort.by(direction, orderBy);
        final var pageable = PageRequest.of(pageNumber, pageSize, sort);
        final var matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final var example = Example.of(role, matcher);
        final var result = repository.findAll(example, pageable);
        log.debug("Roles fetched: totalElements={}, totalPages={}", result.getTotalElements(), result.getTotalPages());
        return result;
    }

    public Long count(Role role) {
        log.debug("Counting roles");
        final var matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final var example = Example.of(role, matcher);
        final var result = repository.count(example);
        log.debug("Roles count: {}", result);
        return result;
    }

}
