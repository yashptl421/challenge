package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @BeforeEach
    void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    void createAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        Account account = accountsService.getAccount("Id-123");
        assertThat(account.getAccountId()).isEqualTo("Id-123");
        assertThat(account.getBalance()).isEqualByComparingTo("1000");
    }

    @Test
    void createDuplicateAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNoAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNoBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNoBody() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNegativeBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void createAccountEmptyAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    void getAccount() throws Exception {
        String uniqueAccountId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
        this.accountsService.createAccount(account);
        this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
    }

    @Test
    void fundTransfer() throws Exception {
        Account a1 = new Account("Id-123", new BigDecimal(5000));
        Account a2 = new Account("Id-124", new BigDecimal(5000));

        accountsService.getAccountsRepository().createAccount(a1);
        accountsService.getAccountsRepository().createAccount(a2);

        this.mockMvc.perform(put("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-124\", \"amount\":1000}")).andExpect(status().isCreated());

        Account fromAccount = accountsService.getAccount("Id-123");
        Account toAccount = accountsService.getAccount("Id-124");
        assertThat(fromAccount.getBalance()).isEqualTo(new BigDecimal(4000));
        assertThat(toAccount.getBalance()).isEqualTo(new BigDecimal(6000));
    }

    @Test
    void fundTransferWithNoAmount() throws Exception {
        this.mockMvc.perform(put("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-124\", \"amount\":0}")).andExpect(status().isBadRequest());
    }
    @Test
    void fundTransferWithNegativeAmount() throws Exception {
        this.mockMvc.perform(put("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"Id-123\",\"toAccountId\":\"Id-124\", \"amount\":-880}")).andExpect(status().isBadRequest());
    }
    @Test
    void fundTransferWithEmptyAccountId() throws Exception {
        this.mockMvc.perform(put("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"\",\"toAccountId\":\"Id-124\", \"amount\":880}")).andExpect(status().isBadRequest());
    }
}
