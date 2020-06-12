package com.sergiovitorino.springbootjwt.domain.repository;

import com.sergiovitorino.springbootjwt.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
