package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Account {

    @NotNull
    @NotEmpty
    private final String accountId;
    @JsonIgnore
    private final Lock lock = new ReentrantLock();

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal balance;

    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = BigDecimal.ZERO;
    }

    @JsonCreator
    public Account(@JsonProperty("accountId") String accountId,
                   @JsonProperty("balance") BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }
}
