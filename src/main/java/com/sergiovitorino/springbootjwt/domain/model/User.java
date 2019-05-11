package com.sergiovitorino.springbootjwt.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-binary")
    private UUID id;

    private String name;
    private String email;

    @Column(length=10485760)
    private String lastToken;

    @JsonIgnore
    private String password;

    @Getter(AccessLevel.NONE)
    private Boolean enabled;

    @ManyToOne
    @JoinColumn(nullable = false, name = "role_id")
    private Role role;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String[] authorities = new String[role.getAuthorities().size()];
        for(int i = 0; i < role.getAuthorities().size(); i++)
            authorities[i] = role.getAuthorities().get(i).getName();
        return AuthorityUtils.createAuthorityList(authorities);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (enabled == null)
            enabled = false;
        return enabled;
    }

    public String toString(){
        return email;
    }
}