package com.bankServer.controller;

import com.bankServer.model.Account;
import com.bankServer.model.Transaction;
import com.bankServer.repository.AccountRepository;
import com.bankServer.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/accounts/{accountId}/transactions")
    public List<Map<String, Object>> getTransactions(@PathVariable Long accountId) {
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");

        // Map the transactions to a list of formatted results
        List<Map<String, Object>> formattedTransactions = transactions.stream()
                .map(transaction -> {
                    Map<String, Object> transactionMap = new HashMap<>();
                    transactionMap.put("id", transaction.getTransactionId());
                    transactionMap.put("amount", transaction.getAmount());
                    transactionMap.put("sourceAccount", transaction.getSourceAccount());
                    transactionMap.put("destinationAccount", transaction.getDestinationAccount());
                    transactionMap.put("type", transaction.getType());
                    // Format the timestamp before returning
                    transactionMap.put("timestamp", transaction.getTimestamp());
                    return transactionMap;
                })
                .collect(Collectors.toList());

        return formattedTransactions;
    }

    @PostMapping("/accounts/{accountId}/transactions")
    public Transaction createTransaction(@PathVariable Long accountId, @RequestBody Map<String, Object> transactionData) {
        System.out.println("Received transaction: " + transactionData);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for ID: " + accountId));

        // Create a new Transaction instance and set fields manually
        Transaction transaction = new Transaction();
        transaction.setAccount(account);

        if (transactionData.get("type") != null) {
            transaction.setType(Transaction.TransactionType.valueOf(transactionData.get("type").toString()));
            System.out.println("Transaction type set to: " + transaction.getType());
        } else {
            throw new IllegalArgumentException("Transaction type is required");
        }

        // Use sourceAccount and destinationAccount to match frontend keys
        if (transactionData.containsKey("sourceAccount")) {
            transaction.setSourceAccount(Account.AccountType.valueOf(transactionData.get("sourceAccount").toString()));
            System.out.println("Source account type set to: " + transaction.getSourceAccount());
        } else {
            throw new IllegalArgumentException("Source account type is required");
        }

        if (transactionData.containsKey("destinationAccount")) {
            transaction.setDestinationAccount(Account.AccountType.valueOf(transactionData.get("destinationAccount").toString()));
            System.out.println("Destination account type set to: " + transaction.getDestinationAccount());
        } else {
            transaction.setDestinationAccount(transaction.getSourceAccount());
        }

        // Set amount and timestamp
        transaction.setAmount(Double.parseDouble(transactionData.get("amount").toString()));

        if (transactionData.containsKey("timestamp") && transactionData.get("timestamp") != null) {
            Instant timestampInstant = Instant.parse(transactionData.get("timestamp").toString());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a").withZone(ZoneId.of("America/Chicago"));
            transaction.setTimestamp(formatter.format(timestampInstant));
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            transaction.setTimestamp(LocalDateTime.now(ZoneId.of("America/Chicago")).format(formatter));
        }

        System.out.println("Processed transaction: " + transaction);
        return transactionService.createTransaction(transaction);
    }
}
