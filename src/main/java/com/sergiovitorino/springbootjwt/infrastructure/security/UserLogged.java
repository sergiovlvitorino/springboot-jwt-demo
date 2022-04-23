package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class UserLogged {

    public UUID getUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
