package com.sergiovitorino.springbootjwt.application.service;

import com.sergiovitorino.springbootjwt.domain.exception.EmailAlreadyExistsException;
import com.sergiovitorino.springbootjwt.domain.exception.ResourceNotFoundException;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import com.sergiovitorino.springbootjwt.infrastructure.security.UserLogged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserLogged userLogged;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, UserLogged userLogged) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userLogged = userLogged;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var user = repository.findByEmail(email);
        if (user == null) {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + email);
        }
        return user;
    }

    public Page<User> findAll(Integer pageNumber, Integer pageSize, String orderBy, Boolean asc, User user) {
        final var direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sort = Sort.by(direction, orderBy);
        final var pageable = PageRequest.of(pageNumber, pageSize, sort);
        var matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase();
        if (user.getEnabled() == null) {
            matcher = matcher.withIgnorePaths("enabled");
        }
        final var example = Example.of(user, matcher);
        final var result = repository.findAll(example, pageable);

        return result;
    }

    public Long count(User user) {
        var matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        if (user.getEnabled() == null) {
            matcher = matcher.withIgnorePaths("enabled");
        }
        final var example = Example.of(user, matcher);
        final var result = repository.count(example);

        return result;
    }

    public User save(User user) {
        var old = repository.findByEmail(user.getEmail());
        if (old != null) {
            log.warn("Attempt to create user with existing email: {}", user.getEmail());
            throw new EmailAlreadyExistsException("E-mail already");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDateCreatedAt(LocalDateTime.now());
        UUID creatorId;
        try {
            creatorId = userLogged.getUserId();
            user.setUserIdCreatedAt(creatorId);
        } catch (Exception e) {
            log.warn("Could not get logged user ID for audit, using system ID: {}", e.getMessage());
            creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            user.setUserIdCreatedAt(creatorId);
        }
        user.setEnabled(true);
        User savedUser = repository.save(user);
        log.info("User created: id={}, email={}, createdBy={}", savedUser.getId(), savedUser.getEmail(), creatorId);
        return savedUser;
    }

    public User update(User user) {
        var old = repository.findById(user.getId()).orElse(null);
        if (old == null) {
            log.warn("Attempt to update non-existent user: id={}", user.getId());
            throw new ResourceNotFoundException("User not found");
        }
        String oldName = old.getName();
        old.setName(user.getName());
        old.setDateUpdatedAt(LocalDateTime.now());
        UUID updaterId;
        try {
            updaterId = userLogged.getUserId();
            old.setUserIdUpdatedAt(updaterId);
        } catch (Exception e) {
            log.warn("Could not get logged user ID for audit, using system ID: {}", e.getMessage());
            updaterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            old.setUserIdUpdatedAt(updaterId);
        }
        User updatedUser = repository.save(old);
        log.info("User updated: id={}, nameChanged='{}'->'{}', updatedBy={}", updatedUser.getId(), oldName, updatedUser.getName(), updaterId);
        return updatedUser;
    }

    public User disable(UUID id) {
        var user = repository.findById(id).orElse(null);
        if (user == null) {
            log.warn("Attempt to disable non-existent user: id={}", id);
            throw new ResourceNotFoundException("User not found");
        }
        user.setEnabled(false);
        user.setDateDisabledAt(LocalDateTime.now());
        UUID disablerId;
        try {
            disablerId = userLogged.getUserId();
            user.setUserIdDisabledAt(disablerId);
        } catch (Exception e) {
            log.warn("Could not get logged user ID for audit, using system ID: {}", e.getMessage());
            disablerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            user.setUserIdDisabledAt(disablerId);
        }
        User disabledUser = repository.save(user);
        log.info("User disabled: id={}, email={}, disabledBy={}", disabledUser.getId(), disabledUser.getEmail(), disablerId);
        return disabledUser;
    }
}
