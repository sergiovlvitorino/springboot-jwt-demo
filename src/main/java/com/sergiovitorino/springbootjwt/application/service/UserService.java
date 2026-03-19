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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final Set<String> ALLOWED_ORDER_FIELDS = Set.of("name", "email", "enabled", "dateCreatedAt", "dateUpdatedAt");
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

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
        return repository.findByEmailWithAuthorities(email)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Integer pageNumber, Integer pageSize, String orderBy, Boolean asc, User user) {
        String safeOrderBy = ALLOWED_ORDER_FIELDS.contains(orderBy) ? orderBy : "name";
        final var direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var sort = Sort.by(direction, safeOrderBy);
        final var pageable = PageRequest.of(pageNumber, pageSize, sort);
        var matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase();
        if (user.getEnabled() == null) {
            matcher = matcher.withIgnorePaths("enabled");
        }
        matcher = matcher.withIgnorePaths("accountLocked");
        final var example = Example.of(user, matcher);
        return repository.findAll(example, pageable);
    }

    @Transactional(readOnly = true)
    public Long count(User user) {
        var matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase();
        if (user.getEnabled() == null) {
            matcher = matcher.withIgnorePaths("enabled");
        }
        matcher = matcher.withIgnorePaths("accountLocked");
        final var example = Example.of(user, matcher);
        return repository.count(example);
    }

    @Transactional
    public User save(User user) {
        repository.findByEmail(user.getEmail()).ifPresent(existing -> {
            log.warn("Attempt to create user with existing email: {}", user.getEmail());
            throw new EmailAlreadyExistsException("E-mail already");
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDateCreatedAt(LocalDateTime.now());
        user.setUserIdCreatedAt(getAuditUserId());
        user.setEnabled(true);
        user.setAccountLocked(false);
        User savedUser = repository.save(user);
        log.info("User created: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Transactional
    public User update(User user) {
        var old = repository.findById(user.getId()).orElseThrow(() -> {
            log.warn("Attempt to update non-existent user: id={}", user.getId());
            return new ResourceNotFoundException("User not found");
        });
        String oldName = old.getName();
        old.setName(user.getName());
        old.setDateUpdatedAt(LocalDateTime.now());
        old.setUserIdUpdatedAt(getAuditUserId());
        User updatedUser = repository.save(old);
        log.info("User updated: id={}, nameChanged='{}'->'{}'" , updatedUser.getId(), oldName, updatedUser.getName());
        return updatedUser;
    }

    @Transactional
    public User disable(UUID id) {
        var user = repository.findById(id).orElseThrow(() -> {
            log.warn("Attempt to disable non-existent user: id={}", id);
            return new ResourceNotFoundException("User not found");
        });
        user.setEnabled(false);
        user.setDateDisabledAt(LocalDateTime.now());
        user.setUserIdDisabledAt(getAuditUserId());
        User disabledUser = repository.save(user);
        log.info("User disabled: id={}, email={}", disabledUser.getId(), disabledUser.getEmail());
        return disabledUser;
    }

    private UUID getAuditUserId() {
        try {
            UUID userId = userLogged.getUserId();
            return userId != null ? userId : SYSTEM_USER_ID;
        } catch (Exception e) {
            log.warn("Could not get logged user ID for audit: {}", e.getMessage());
            return SYSTEM_USER_ID;
        }
    }
}
