package com.sergiovitorino.springbootjwt.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Authority {

    @Id
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(java.sql.Types.BINARY)
    private UUID id;
    private String name;
    @JoinTable(name = "role_authority", joinColumns = {
            @JoinColumn(name = "authority_id", referencedColumnName = "id")}, inverseJoinColumns = {
            @JoinColumn(name = "role_id", referencedColumnName = "id")})
    @ManyToMany(cascade = CascadeType.DETACH)
    @JsonIgnore
    private List<Role> roles;

    public Authority() {
    }

    public Authority(UUID id, String name, List<Role> roles) {
        this.id = id;
        this.name = name;
        this.roles = roles;
    }

    public Authority(String name) {
        this.name = name;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return java.util.Objects.equals(id, authority.id) &&
               java.util.Objects.equals(name, authority.name) &&
               java.util.Objects.equals(roles, authority.roles);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, roles);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
