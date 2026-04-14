package com.sergiovitorino.springbootjwt.domain.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_token_user_id", columnList = "user_id")
})
public class RefreshToken {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(Types.BINARY)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    @JdbcTypeCode(Types.BINARY)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public RefreshToken() {
    }

    public RefreshToken(String token, UUID userId, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
