package com.sergiovitorino.springbootjwt.domain.repository;

import com.sergiovitorino.springbootjwt.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.used = true, r.usedAt = :ts WHERE r.userId = :userId AND r.used = false")
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("ts") LocalDateTime ts);
}
