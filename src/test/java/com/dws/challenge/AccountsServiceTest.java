package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @BeforeEach
    void setUp() {
        accountsService.getAccountsRepository().clearAccounts();
        Account a1 = new Account("Id-123", new BigDecimal(500));
        Account a2 = new Account("Id-124", new BigDecimal(500));
        Account a3 = new Account("Id-125", new BigDecimal(500));
        Account a8 = new Account("Id-128", new BigDecimal(500));
        Account a9 = new Account("Id-129", new BigDecimal(500));
        this.accountsService.createAccount(a1);
        this.accountsService.createAccount(a2);
        this.accountsService.createAccount(a3);
        this.accountsService.createAccount(a8);
        this.accountsService.createAccount(a9);

    }

    @Test
    void addAccount() {
        Account account = new Account("Id-126");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-126")).isEqualTo(account);
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
        FundTransfer fundTransfer = new FundTransfer("Id-124", "Id-125", new BigDecimal(500));
        this.accountsService.fundTransfer(fundTransfer);
        assertThat(this.accountsService.getAccount("Id-125").getBalance()).isEqualTo(new BigDecimal(1000));
    }

    @Test
    void invalidAmountTransfer() {
        FundTransfer fundTransfer = new FundTransfer("Id-124", "Id-125", new BigDecimal(-500));
        try {
            this.accountsService.fundTransfer(fundTransfer);
        } catch (InvalidAmountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid amount, negative or zero value not acceptable");
        }
    }

    @Test
    void insufficientBalanceFundTransfer() {
        FundTransfer fundTransfer = new FundTransfer("Id-124", "Id-125", new BigDecimal(50000));
        try {
            this.accountsService.fundTransfer(fundTransfer);
        } catch (InvalidAmountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Insufficient Balance to make transaction against account id[Id-124]");
        }
    }

    @Test
    void testConcurrentTransfers() throws InterruptedException {
        Runnable transfer1 = () -> accountsService.fundTransfer(new FundTransfer("Id-123", "Id-124", new BigDecimal(100)));
        Runnable transfer2 = () -> accountsService.fundTransfer(new FundTransfer("Id-124", "Id-125", new BigDecimal(200)));
        Runnable transfer3 = () -> accountsService.fundTransfer(new FundTransfer("Id-125", "Id-123", new BigDecimal(300)));
        Runnable transfer4 = () -> accountsService.fundTransfer(new FundTransfer("Id-128", "Id-129", new BigDecimal(500)));

        Thread t1 = new Thread(transfer1);
        Thread t2 = new Thread(transfer2);
        Thread t3 = new Thread(transfer3);
        Thread t4 = new Thread(transfer4);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();

        assertEquals(new BigDecimal(700), accountsService.getAccount("Id-123").getBalance());
        assertEquals(new BigDecimal(400), accountsService.getAccount("Id-124").getBalance());
        assertEquals(new BigDecimal(400), accountsService.getAccount("Id-125").getBalance());
        assertEquals(new BigDecimal(1000), accountsService.getAccount("Id-129").getBalance());
    }
}
