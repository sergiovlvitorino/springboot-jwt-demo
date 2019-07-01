package com.sergiovitorino.springbootjwt.application;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import com.sergiovitorino.springbootjwt.domain.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired private RoleRepository repository;

    public Page<Role> findAll(Integer pageNumber, Integer pageSize, String orderBy, Boolean asc, Role role) {
        final Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final Sort sort = Sort.by(direction, orderBy);
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        final ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final Example<Role> example = Example.of(role, matcher);
        return repository.findAll(example, pageable);
    }

    public Long count(Role role) {
        final ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final Example<Role> example = Example.of(role, matcher);
        return repository.count(example);
    }

}
