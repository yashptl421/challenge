package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.FundTransfer;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
        notificationService.notifyAboutTransfer(accountsRepository.fundTransfer(fundTransfer), "Fund transfer has been completed successfully");
    }
}
