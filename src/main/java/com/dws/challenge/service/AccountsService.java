package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundTransfer;
import com.dws.challenge.exception.InvalidAmountException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;
    @Getter
    private final NotificationService notificationService;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, EmailNotificationService emailNotificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = emailNotificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public void fundTransfer(FundTransfer fundTransfer) {
        // write operation is locked for accounts

        Account fromAccount = accountsRepository.getAccount(fundTransfer.getFromAccountId());
        Account toAccount = accountsRepository.getAccount(fundTransfer.getToAccountId());
        if (fundTransfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {     // if amount is less then or equal to 0 throw exception
            throw new InvalidAmountException(
                    "Invalid amount, negative or zero value not acceptable");
        }
        if (fundTransfer.getAmount().compareTo(fromAccount.getBalance()) > 0) {     // if user account has less amount then transfer amount throw exception
            throw new InvalidAmountException(
                    "Insufficient Balance to make transaction against account id[" + fromAccount.getAccountId() + "]");
        }
        Account firstAccountLock = fromAccount.getAccountId().compareTo(toAccount.getAccountId()) < 0 ? fromAccount : toAccount;
        Account secondAccountLock = firstAccountLock == fromAccount ? toAccount : fromAccount;

              // Lock at account level
        firstAccountLock.getLock().lock();      //first account lock
        try {
            secondAccountLock.getLock().lock();  //second account lock
            try {
                fromAccount.setBalance(fromAccount.getBalance().subtract(fundTransfer.getAmount()));
                toAccount.setBalance(toAccount.getBalance().add(fundTransfer.getAmount()));
            } finally {
                secondAccountLock.getLock().unlock();
            }
        } finally {
            firstAccountLock.getLock().unlock();
        }

        notificationService.notifyAboutTransfer(fromAccount, "Fund Transfer Successfully ");
    }
}
