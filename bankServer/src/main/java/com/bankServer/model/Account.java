package com.bankServer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    @Column(name = "balance")
    private double balance;

    public Account(User user, AccountType accountType, double balance) {
        this.user = user;
        this.accountType = accountType;
        this.balance = balance;
    }

    public enum AccountType {
        CHECKING,
        SAVINGS
    }
}

