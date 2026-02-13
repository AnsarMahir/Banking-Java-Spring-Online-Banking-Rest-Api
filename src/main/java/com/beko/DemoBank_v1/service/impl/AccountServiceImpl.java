package com.beko.DemoBank_v1.service.impl;

import com.beko.DemoBank_v1.helpers.GenAccountNumber;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.AccountRepository;
import com.beko.DemoBank_v1.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public ResponseEntity createAccount(Map<String, String> requestMap, User user) {
        try {
            validateInputFields(requestMap);

            String accountName = requestMap.get("account_name");
            String accountType = requestMap.get("account_type");

            // CRITICAL FIX (V-13): generateAccountNumber() now returns String
            String bankAccountNumber = GenAccountNumber.generateAccountNumber();

            long userId = user.getUser_id();

            accountRepository.createBankAccount(userId, bankAccountNumber, accountName, accountType);

            logger.info("Account created successfully for user: {}, account number: {}", userId, bankAccountNumber);
            return ResponseEntity.ok(accountRepository.getUserAccountsById(userId));

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error creating account: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // CRITICAL FIX (V-20): Don't expose internal error details
            logger.error("Error creating account for user: {}", user.getUser_id(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating account. Please try again.");
        }
    }

    private void validateInputFields(Map<String, String> requestMap) {
        String accountName = requestMap.get("account_name");
        String accountType = requestMap.get("account_type");

        if (accountName == null || accountName.isEmpty() || accountType == null || accountType.isEmpty()) {
            throw new IllegalArgumentException("Account name and type cannot be empty!");
        }
    }
}