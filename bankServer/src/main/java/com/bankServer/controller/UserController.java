package com.bankServer.controller;

import com.bankServer.model.Account;
import com.bankServer.model.User;
import com.bankServer.repository.AccountRepository;
import com.bankServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") int userId) {
        Optional<User> userData = userRepository.findById(userId);
        if (userData.isPresent()) {
            return new ResponseEntity<>(userData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("users/{userId}/accounts")
    public ResponseEntity<List<Account>> getUserAccounts(@PathVariable int userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            List<Account> accounts = accountRepository.findByUser_UserId(userId);
            return new ResponseEntity<>(accounts, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/{userId}/accounts/{accountId}")
    public ResponseEntity<Account> getAccountById(@PathVariable("userId") int userId, @PathVariable("accountId") Long accountId) {
        try {
            Optional<Account> account = accountRepository.findById(accountId);
            if (account.isPresent() && account.get().getUser().getUserId() == userId) {
                return new ResponseEntity<>(account.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<User> newUser(@RequestBody Map<String, Object> userData) {
        try {
            User user = new User(
                    userData.get("firstName").toString(),
                    userData.get("lastName").toString(),
                    userData.get("email").toString(),
                    userData.get("password").toString(),
                    Integer.parseInt(userData.get("pin").toString())
            );
            userRepository.save(user);

            String accountType = userData.get("accountType").toString();
            if (accountType.equals("checking") || accountType.equals("both")) {
                double checkingBalance = Double.parseDouble(userData.get("checkingBalance").toString());
                Account checkingAccount = new Account(user, Account.AccountType.CHECKING, checkingBalance);
                accountRepository.save(checkingAccount);
            }
            if (accountType.equals("savings") || accountType.equals("both")) {
                double savingsBalance = Double.parseDouble(userData.get("savingsBalance").toString());
                Account savingsAccount = new Account(user, Account.AccountType.SAVINGS, savingsBalance);
                accountRepository.save(savingsAccount);
            }

            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PostMapping("/users")
//    public ResponseEntity<User> newUser(@RequestBody User user) {
//        try {
//
//            // Save the user, and accounts will be automatically saved due to CascadeType.ALL
//            User savedUser = userRepository.save(user);
//
//            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
//        } catch (Exception e) {
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticateUser(@RequestBody User loginRequest) {
        try {
            Map<String, Object> response = new HashMap<>();
            Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());

            if (user.isPresent() && user.get().getPin() == loginRequest.getPin()) {
                response.put("authenticated", true);
                response.put("userId", user.get().getUserId());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("authenticated", false);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/users/{userId}/transfer")
    public ResponseEntity<Map<String, Object>> transferFunds(
            @PathVariable("userId") int userId,
            @RequestBody Map<String, Object> transferRequest) {

        Long sourceAccountId = Long.parseLong(transferRequest.get("sourceAccountId").toString());
        Long destinationAccountId = Long.parseLong(transferRequest.get("destinationAccountId").toString());
        Double amount = Double.parseDouble(transferRequest.get("amount").toString());

        if (sourceAccountId.equals(destinationAccountId)) {
            return new ResponseEntity<>(Map.of("message", "Source and destination accounts cannot be the same."), HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<Account> sourceAccountOpt = accountRepository.findById(sourceAccountId);
            Optional<Account> destinationAccountOpt = accountRepository.findById(destinationAccountId);

            if (!sourceAccountOpt.isPresent() || !destinationAccountOpt.isPresent()) {
                return new ResponseEntity<>(Map.of("message", "One or both accounts not found."), HttpStatus.NOT_FOUND);
            }

            Account sourceAccount = sourceAccountOpt.get();
            Account destinationAccount = destinationAccountOpt.get();

            if (sourceAccount.getBalance() < amount) {
                return new ResponseEntity<>(Map.of("message", "Insufficient balance in source account."), HttpStatus.BAD_REQUEST);
            }

            sourceAccount.setBalance(sourceAccount.getBalance() - amount);
            destinationAccount.setBalance(destinationAccount.getBalance() + amount);

            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            return new ResponseEntity<>(Map.of("message", "Transfer successful!"), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "An error occurred during the transfer."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable("userId") int userId, @RequestBody User user) {
        try {
            Optional<User> userData = userRepository.findById(userId);

            if (userData.isPresent()) {
                User _user = userData.get();
                _user.setFirstName(user.getFirstName());
                _user.setLastName(user.getLastName());
                _user.setEmail(user.getEmail());
                _user.setPassword(user.getPassword());
                _user.setPin(user.getPin());
                return new ResponseEntity<>(userRepository.save(_user), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/users/{userId}/accounts/{accountId}")
    public ResponseEntity<Account> updateAccount(@PathVariable("userId") int userId, @PathVariable("accountId") Long accountId, @RequestBody Account updatedAccount) {
        try {
            Optional<Account> accountData = accountRepository.findById(accountId);
            if (accountData.isPresent()) {
                Account account = accountData.get();
                account.setBalance(updatedAccount.getBalance()); // Update balance or other fields if needed
                return new ResponseEntity<>(accountRepository.save(account), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("userId") int userId) {
        try {
            userRepository.deleteById(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<HttpStatus> deleteAllUsers() {
        try {
            userRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
