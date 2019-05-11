package com.sergiovitorino.springbootjwt.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sergiovitorino.springbootjwt.domain.model.Authority;

import java.util.UUID;

public interface AuthorityRepository extends JpaRepository<Authority, UUID> {
}
