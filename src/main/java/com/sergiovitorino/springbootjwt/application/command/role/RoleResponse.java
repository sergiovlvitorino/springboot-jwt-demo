package com.sergiovitorino.springbootjwt.application.command.role;

import com.sergiovitorino.springbootjwt.domain.model.Role;

import java.util.List;
import java.util.UUID;

public record RoleResponse(
    UUID id,
    String name,
    List<String> authorities
) {
    public static RoleResponse from(Role role) {
        List<String> authorityNames = role.getAuthorities().stream()
            .map(authority -> authority.getName())
            .toList();
        return new RoleResponse(
            role.getId(),
            role.getName(),
            authorityNames
        );
    }
}
