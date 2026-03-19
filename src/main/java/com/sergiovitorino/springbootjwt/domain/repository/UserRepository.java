package com.sergiovitorino.springbootjwt.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sergiovitorino.springbootjwt.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role r LEFT JOIN FETCH r.authorities WHERE u.email = :email")
    Optional<User> findByEmailWithAuthorities(@Param("email") String email);

}
