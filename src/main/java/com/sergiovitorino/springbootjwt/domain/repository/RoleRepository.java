package com.sergiovitorino.springbootjwt.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sergiovitorino.springbootjwt.domain.model.Role;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
