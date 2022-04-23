package com.sergiovitorino.springbootjwt.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Role {

    @Id
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-binary")
    private UUID id;
    private String name;
    @OneToMany(mappedBy = "role", cascade = CascadeType.DETACH)
    @JsonIgnore
    private List<User> users;

    @JoinTable(name = "role_authority", joinColumns = {
            @JoinColumn(name = "role_id", referencedColumnName = "id")}, inverseJoinColumns = {
            @JoinColumn(name = "authority_id", referencedColumnName = "id")})
    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @Getter(AccessLevel.NONE)
    private List<Authority> authorities;

    public Role(String name, List<Authority> authorities) {
        this.name = name;
        this.authorities = authorities;
    }

    public Role(UUID id) {
        this.id = id;
    }

    public List<Authority> getAuthorities() {
        if (authorities == null)
            authorities = new ArrayList<>();
        return authorities;
    }
}
