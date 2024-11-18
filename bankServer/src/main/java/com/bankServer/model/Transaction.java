package com.bankServer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "accountId")
    private Account account;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "sourceAccount")
    @Enumerated(EnumType.STRING)
    private Account.AccountType sourceAccount;

    @Column(name = "destinationAccount")
    @Enumerated(EnumType.STRING)
    private Account.AccountType destinationAccount;

    @Column(name = "amount")
    private double amount;

    @Column(name = "dateTime")
    private String timestamp;

    public enum TransactionType{
        DEPOSIT,
        WITHDRAW,
        TRANSFER
    }

    public Transaction(Account account, TransactionType type, Account.AccountType sourceAccount, Account.AccountType destinationAccount, double amount, String timestamp) {
        this.account = account;
        this.type = type;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}

