package com.sergiovitorino.springbootjwt.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class UserLogged {

    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (IllegalArgumentException e) {
                // Logar erro ou retornar nulo/padrão se o principal não for um UUID válido
                return null;
            }
        }
        return null;
    }

}
