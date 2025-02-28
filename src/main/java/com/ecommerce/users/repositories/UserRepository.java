package com.ecommerce.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecommerce.users.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Changed return type
    Optional<User> findByToken(String token); // Renamed and corrected return type
}