package com.sergiovitorino.springbootjwt.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Authority {

    @Id
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-binary")
    private UUID id;
    private String name;
    @JoinTable(name = "role_authority", joinColumns = {
            @JoinColumn(name = "authority_id", referencedColumnName = "id") }, inverseJoinColumns = {
            @JoinColumn(name = "role_id", referencedColumnName = "id") })
    @ManyToMany(cascade = CascadeType.DETACH)
    @JsonIgnore
    private List<Role> roles;

    public Authority(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }

}
