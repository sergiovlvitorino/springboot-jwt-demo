package com.sergiovitorino.springbootjwt.service;

import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.infrastructure.security.UserLogged;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserLogged userLogged;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Page<User> findAll(Integer pageNumber, Integer pageSize, String orderBy, Boolean asc, User user) {
        final Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final Sort sort = Sort.by(direction, orderBy);
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        final ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final Example<User> example = Example.of(user, matcher);
        return repository.findAll(example, pageable);
    }

    public Long count(User user) {
        final ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        final Example<User> example = Example.of(user, matcher);
        return repository.count(example);
    }

    public User save(User user) {
        User old = repository.findByEmail(user.getEmail()).orElse(null);
        if (old != null)
            throw new IllegalArgumentException("E-mail already");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public User update(User user) {
        User old = repository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        old.setName(user.getName());
        return repository.save(old);
    }

    public User disable(UUID id) {
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(false);
        return repository.save(user);
    }
}