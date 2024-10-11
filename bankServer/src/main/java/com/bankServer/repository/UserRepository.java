package com.bankServer.repository;

import com.bankServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByLastName(String lastName);
    Optional<User> findByEmail(String email);
}
