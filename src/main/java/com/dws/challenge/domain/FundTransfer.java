package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundTransfer {

    @NotNull
    @NotEmpty
    private final String fromAccountId;
    @NotNull
    @NotEmpty
    private final String toAccountId;

    @NotNull
    @Min(value = 0, message = "amount must be positive.")
    private BigDecimal amount;

    public FundTransfer(String fromAccountId, String toAccountId) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = BigDecimal.ZERO;
    }

    @JsonCreator
    public FundTransfer(@JsonProperty("fromAccountId") String fromAccountId, @JsonProperty("toAccountId") String toAccountId,
                   @JsonProperty("amount") BigDecimal amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }
}
