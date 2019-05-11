package com.sergiovitorino.springbootjwt.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sergiovitorino.springbootjwt.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

}
