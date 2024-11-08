package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InvalidAmountException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock(); // retrive only write lock

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public Account fundTransfer(FundTransfer fundTransfer) {
        writeLock.lock();                                               // write operation is locked for accounts
        try {
            Account fromAccount = getAccount(fundTransfer.getFromAccountId());
            Account toAccount = getAccount(fundTransfer.getToAccountId());
            if (fundTransfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {     // if amount is less then or equal to 0 throw exception
                throw new InvalidAmountException(
                        "Invalid amount, negative or zero value not acceptable");
            }
            if (fundTransfer.getAmount().compareTo(fromAccount.getBalance()) > 0) {     // if user account has less amount then transfer amount throw exception
                throw new InvalidAmountException(
                        "Insufficient Balance to make transaction against account id[" + fromAccount.getAccountId() + "]");
            }
            fromAccount.setBalance(fromAccount.getBalance().subtract(fundTransfer.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(fundTransfer.getAmount()));
            accounts.put(toAccount.getAccountId(), toAccount);
            return accounts.put(fromAccount.getAccountId(), fromAccount);
        } finally {
            writeLock.unlock();
        }
    }

}
