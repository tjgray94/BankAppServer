package com.bankServer.repository;

import com.bankServer.model.Account;
import com.bankServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // Custom query methods (if needed) can be defined here
    List<Account> findByUser(User user);
    List<Account> findByUser_UserId(int userId);
    Optional<Account> findById(Long accountId);
}