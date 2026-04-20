package com.decms.repository;

import com.decms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Supports Spring Security UserDetailsService implementation.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (used by Spring Security).
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);
}
