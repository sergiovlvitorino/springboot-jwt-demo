package com.sergiovitorino.springbootjwt.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreatedAt;
    private LocalDateTime dateUpdatedAt;
    private LocalDateTime dateDisabledAt;
    @Column(updatable = false)
    private UUID userIdCreatedAt;
    private UUID userIdUpdatedAt;
    private UUID userIdDisabledAt;

    @PrePersist
    public void onPrePersist() {
        this.dateCreatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public LocalDateTime getDateCreatedAt() {
        return dateCreatedAt;
    }

    public void setDateCreatedAt(LocalDateTime dateCreatedAt) {
        this.dateCreatedAt = dateCreatedAt;
    }

    public LocalDateTime getDateUpdatedAt() {
        return dateUpdatedAt;
    }

    public void setDateUpdatedAt(LocalDateTime dateUpdatedAt) {
        this.dateUpdatedAt = dateUpdatedAt;
    }

    public LocalDateTime getDateDisabledAt() {
        return dateDisabledAt;
    }

    public void setDateDisabledAt(LocalDateTime dateDisabledAt) {
        this.dateDisabledAt = dateDisabledAt;
    }

    public UUID getUserIdCreatedAt() {
        return userIdCreatedAt;
    }

    public void setUserIdCreatedAt(UUID userIdCreatedAt) {
        this.userIdCreatedAt = userIdCreatedAt;
    }

    public UUID getUserIdUpdatedAt() {
        return userIdUpdatedAt;
    }

    public void setUserIdUpdatedAt(UUID userIdUpdatedAt) {
        this.userIdUpdatedAt = userIdUpdatedAt;
    }

    public UUID getUserIdDisabledAt() {
        return userIdDisabledAt;
    }

    public void setUserIdDisabledAt(UUID userIdDisabledAt) {
        this.userIdDisabledAt = userIdDisabledAt;
    }

    @Override
    public String toString() {
        return "AbstractEntity{" +
                "dateCreatedAt=" + dateCreatedAt +
                ", dateUpdatedAt=" + dateUpdatedAt +
                ", dateDisabledAt=" + dateDisabledAt +
                ", userIdCreatedAt=" + userIdCreatedAt +
                ", userIdUpdatedAt=" + userIdUpdatedAt +
                ", userIdDisabledAt=" + userIdDisabledAt +
                '}';
    }
}
