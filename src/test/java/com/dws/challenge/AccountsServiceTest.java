package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InvalidAmountException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    @Test
    void addAccount() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    @Test
    void fundTransfer() {

        Account a2 = new Account("Id-124", new BigDecimal(500));
        Account a3 = new Account("Id-125", new BigDecimal(50000));
        this.accountsService.createAccount(a2);
        this.accountsService.createAccount(a3);

        FundTransfer fundTransfer = new FundTransfer("Id-123", "Id-124", new BigDecimal(500));
        this.accountsService.fundTransfer(fundTransfer);
        Account account = new Account("Id-124", new BigDecimal(1000));
        assertThat(this.accountsService.getAccount("Id-124")).isEqualTo(account);
    }

    @Test
    void invalideAmountTransfer() {
        Account a2 = new Account("Id-126", new BigDecimal(500));
        Account a3 = new Account("Id-127", new BigDecimal(50000));
        this.accountsService.createAccount(a2);
        this.accountsService.createAccount(a3);

        FundTransfer fundTransfer = new FundTransfer("Id-126", "Id-127", new BigDecimal(50));

        try {
            this.accountsService.fundTransfer(fundTransfer);
            Account account = new Account("Id-124", new BigDecimal(500));
        } catch (InvalidAmountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid amount, negative or zero value not acceptable");
        }
    }
    @Test
    void insufficientBalanceFundTransfer() {

        FundTransfer fundTransfer = new FundTransfer("Id-126", "Id-127", new BigDecimal(70000));

        try {
            this.accountsService.fundTransfer(fundTransfer);
            Account account = new Account("Id-124", new BigDecimal(500));
        } catch (InvalidAmountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Insufficient Balance to make transaction against account id[Id-126]");
        }
    }
}
