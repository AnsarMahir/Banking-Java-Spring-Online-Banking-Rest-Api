package com.beko.DemoBank_v1.service.impl;

import com.beko.DemoBank_v1.models.Account;
import com.beko.DemoBank_v1.models.PaymentHistory;
import com.beko.DemoBank_v1.models.TransactionHistory;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.AccountRepository;
import com.beko.DemoBank_v1.repository.PaymentHistoryRepository;
import com.beko.DemoBank_v1.repository.TransactHistoryRepository;
import com.beko.DemoBank_v1.service.AppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppServiceImpl implements AppService {

    private static final Logger logger = LoggerFactory.getLogger(AppServiceImpl.class);

    private final AccountRepository accountRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final TransactHistoryRepository transactHistoryRepository;

    @Autowired
    public AppServiceImpl(AccountRepository accountRepository, PaymentHistoryRepository paymentHistoryRepository,
                          TransactHistoryRepository transactHistoryRepository) {
        this.accountRepository = accountRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.transactHistoryRepository = transactHistoryRepository;
    }

    @Override
    public ResponseEntity<?> getDashboard(User user) {
        try {
            long userId = user.getUser_id();
            List<Account> userAccounts = accountRepository.getUserAccountsById(userId);
            BigDecimal totalAccountsBalance = accountRepository.getTotalBalance(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userAccounts", userAccounts);
            response.put("totalBalance", totalAccountsBalance);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // CRITICAL FIX (V-20): Don't expose internal error details
            logger.error("Error fetching dashboard for user {}", user.getUser_id(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard data.");
        }
    }

    @Override
    public ResponseEntity<?> getPaymentHistory(User user) {
        try {
            long userId = user.getUser_id();
            List<PaymentHistory> userPaymentHistory = paymentHistoryRepository.getPaymentsRecordsById(userId);

            Map<String, List> response = new HashMap<>();
            response.put("payment_history", userPaymentHistory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // CRITICAL FIX (V-20): Generic error message
            logger.error("Error fetching payment history for user {}", user.getUser_id(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching payment history.");
        }
    }

    @Override
    public ResponseEntity<?> getTransactionHistory(User user) {
        try {
            long userId = user.getUser_id();
            List<TransactionHistory> userTransactionHistory = transactHistoryRepository
                    .getTransactionRecordsById(userId);

            Map<String, List> response = new HashMap<>();
            response.put("transaction_history", userTransactionHistory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // CRITICAL FIX (V-20): Generic error message
            logger.error("Error fetching transaction history for user {}", user.getUser_id(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching transaction history.");
        }
    }

    @Override
    public ResponseEntity<?> getAccountTransactionHistory(Map<String, String> requestMap, User user) {
        try {
            String accountIdStr = requestMap.get("account_id");

            if (accountIdStr == null || accountIdStr.isEmpty()) {
                return ResponseEntity.badRequest().body("Account ID is required.");
            }

            int accountId = Integer.parseInt(accountIdStr);
            long userId = user.getUser_id();

            // CRITICAL FIX (V-05): Verify account ownership before returning transaction history
            if (!accountRepository.isAccountOwnedByUser(userId, accountId)) {
                logger.warn("Unauthorized access attempt: User {} tried to view transactions for account {}",
                        userId, accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to view this account's transaction history.");
            }

            List<TransactionHistory> accountTransactionHistory = transactHistoryRepository
                    .getTransactionRecordsByAccountId(accountId);

            Map<String, List> response = new HashMap<>();
            response.put("transaction_history", accountTransactionHistory);

            logger.info("User {} accessed transaction history for account {}", userId, accountId);
            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            logger.error("Invalid account ID format", e);
            return ResponseEntity.badRequest().body("Invalid account ID format.");
        } catch (Exception e) {
            // CRITICAL FIX (V-20): Generic error message
            logger.error("Error fetching account transaction history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching account transaction history.");
        }
    }
}