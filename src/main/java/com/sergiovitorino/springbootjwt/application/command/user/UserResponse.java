package com.sergiovitorino.springbootjwt.application.command.user;

import com.sergiovitorino.springbootjwt.domain.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    Boolean enabled,
    String roleName,
    LocalDateTime dateCreatedAt,
    LocalDateTime dateUpdatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getEnabled(),
            user.getRole() != null ? user.getRole().getName() : null,
            user.getDateCreatedAt(),
            user.getDateUpdatedAt()
        );
    }
}
