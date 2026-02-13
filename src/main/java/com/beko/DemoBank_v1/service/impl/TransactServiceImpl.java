package com.beko.DemoBank_v1.service.impl;

import com.beko.DemoBank_v1.models.PaymentRequest;
import com.beko.DemoBank_v1.models.TransferRequest;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.AccountRepository;
import com.beko.DemoBank_v1.repository.TransactRepository;
import com.beko.DemoBank_v1.service.TransactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransactServiceImpl implements TransactService {

    private static final Logger logger = LoggerFactory.getLogger(TransactServiceImpl.class);
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000.00");

    private final AccountRepository accountRepository;
    private final TransactRepository transactRepository;

    @Autowired
    public TransactServiceImpl(AccountRepository accountRepository, TransactRepository transactRepository) {
        this.accountRepository = accountRepository;
        this.transactRepository = transactRepository;
    }

    @Override
    @Transactional
    public ResponseEntity deposit(Map<String, String> requestMap, User user) {
        try {
            validateDepositRequest(requestMap);

            int accountId = Integer.parseInt(requestMap.get("account_id"));
            BigDecimal depositAmount = new BigDecimal(requestMap.get("deposit_amount"));
            long userId = user.getUser_id();

            // CRITICAL FIX (V-03): Verify account ownership
            if (!accountRepository.isAccountOwnedByUser(userId, accountId)) {
                logger.warn("Unauthorized deposit attempt: User {} tried to deposit to account {}", userId, accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to perform this operation.");
            }

            // CRITICAL FIX (V-18): Validate positive amount
            if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Deposit amount must be greater than zero.");
            }

            if (depositAmount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
                return ResponseEntity.badRequest().body("Deposit amount exceeds maximum limit.");
            }

            // CRITICAL FIX (V-09): Use pessimistic locking
            BigDecimal currentBalance = accountRepository.getAccountBalanceWithLock(userId, accountId);
            BigDecimal newBalance = currentBalance.add(depositAmount);

            accountRepository.changeAccountsBalanceById(newBalance, accountId);
            transactRepository.logTransaction(accountId, "deposit", depositAmount.doubleValue(), "online", "success",
                    "Deposit Transaction Successful", LocalDateTime.now());

            logger.info("Successful deposit: User {} deposited {} to account {}", userId, depositAmount, accountId);
            return ResponseEntity.ok(buildDepositResponse(userId));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format.");
        } catch (Exception e) {
            logger.error("Error processing deposit", e);
            return handleException(e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity withdraw(Map<String, String> requestMap, User user) {
        try {
            validateWithdrawalRequest(requestMap);

            int accountId = Integer.parseInt(requestMap.get("account_id"));
            BigDecimal withdrawalAmount = new BigDecimal(requestMap.get("withdrawal_amount"));
            long userId = user.getUser_id();

            // Verify account ownership
            if (!accountRepository.isAccountOwnedByUser(userId, accountId)) {
                logger.warn("Unauthorized withdrawal: User {} tried to withdraw from account {}", userId, accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to perform this operation.");
            }

            if (withdrawalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Withdrawal amount must be greater than zero.");
            }

            if (withdrawalAmount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
                return ResponseEntity.badRequest().body("Withdrawal amount exceeds maximum limit.");
            }

            BigDecimal currentBalance = accountRepository.getAccountBalanceWithLock(userId, accountId);

            if (currentBalance.compareTo(withdrawalAmount) < 0) {
                handleInsufficientFunds(accountId);
                return ResponseEntity.badRequest().body("Insufficient funds.");
            }

            BigDecimal newBalance = currentBalance.subtract(withdrawalAmount);
            accountRepository.changeAccountsBalanceById(newBalance, accountId);
            transactRepository.logTransaction(accountId, "Withdrawal", withdrawalAmount.doubleValue(), "online", "success",
                    "Withdrawal Transaction Successful", LocalDateTime.now());

            logger.info("Successful withdrawal: User {} withdrew {} from account {}", userId, withdrawalAmount, accountId);
            return ResponseEntity.ok(buildWithdrawalResponse(userId));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format.");
        } catch (Exception e) {
            logger.error("Error processing withdrawal", e);
            return handleException(e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity payment(PaymentRequest request, User user) {
        try {
            validatePaymentRequest(request);

            int accountId = Integer.parseInt(request.getAccount_id());
            BigDecimal paymentAmount = new BigDecimal(request.getPayment_amount());
            long userId = user.getUser_id();

            if (!accountRepository.isAccountOwnedByUser(userId, accountId)) {
                logger.warn("Unauthorized payment: User {} tried to pay from account {}", userId, accountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to perform this operation.");
            }

            if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Payment amount must be greater than zero.");
            }

            if (paymentAmount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
                return ResponseEntity.badRequest().body("Payment amount exceeds maximum limit.");
            }

            BigDecimal currentBalance = accountRepository.getAccountBalanceWithLock(userId, accountId);

            if (currentBalance.compareTo(paymentAmount) < 0) {
                handleInsufficientFunds(accountId);
                return ResponseEntity.badRequest().body("Insufficient funds.");
            }

            BigDecimal newBalance = currentBalance.subtract(paymentAmount);
            accountRepository.changeAccountsBalanceById(newBalance, accountId);
            transactRepository.logTransaction(accountId, "Payment", paymentAmount.doubleValue(), "online", "success",
                    "Payment Transaction Successful", LocalDateTime.now());

            logger.info("Successful payment: User {} paid {} from account {}", userId, paymentAmount, accountId);
            return ResponseEntity.ok(buildPaymentResponse(userId));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format.");
        } catch (Exception e) {
            logger.error("Error processing payment", e);
            return handleException(e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity transfer(TransferRequest request, User user) {
        try {
            validateTransferRequest(request);

            int sourceAccountId = Integer.parseInt(request.getSourceAccount());
            int targetAccountId = Integer.parseInt(request.getTargetAccount());
            BigDecimal transferAmount = new BigDecimal(request.getAmount());
            long userId = user.getUser_id();

            if (sourceAccountId == targetAccountId) {
                return ResponseEntity.badRequest().body("Cannot transfer to the same account.");
            }

            // Verify ownership of both accounts
            if (!accountRepository.isAccountOwnedByUser(userId, sourceAccountId)) {
                logger.warn("Unauthorized transfer: User {} tried to transfer from account {}", userId, sourceAccountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to perform this operation.");
            }

            if (!accountRepository.isAccountOwnedByUser(userId, targetAccountId)) {
                logger.warn("Unauthorized transfer: User {} tried to transfer to account {}", userId, targetAccountId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to perform this operation.");
            }

            if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Transfer amount must be greater than zero.");
            }

            if (transferAmount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
                return ResponseEntity.badRequest().body("Transfer amount exceeds maximum limit.");
            }

            // Atomic transfer with locking
            BigDecimal sourceBalance = accountRepository.getAccountBalanceWithLock(userId, sourceAccountId);

            if (sourceBalance.compareTo(transferAmount) < 0) {
                handleInsufficientFunds(sourceAccountId);
                return ResponseEntity.badRequest().body("Insufficient funds.");
            }

            BigDecimal targetBalance = accountRepository.getAccountBalanceWithLock(userId, targetAccountId);

            BigDecimal newSourceBalance = sourceBalance.subtract(transferAmount);
            BigDecimal newTargetBalance = targetBalance.add(transferAmount);

            accountRepository.changeAccountsBalanceById(newSourceBalance, sourceAccountId);
            accountRepository.changeAccountsBalanceById(newTargetBalance, targetAccountId);
            transactRepository.logTransaction(sourceAccountId, "Transfer", transferAmount.doubleValue(), "online", "success",
                    "Transfer Transaction Successful", LocalDateTime.now());

            logger.info("Successful transfer: User {} transferred {} from {} to {}",
                    userId, transferAmount, sourceAccountId, targetAccountId);
            return ResponseEntity.ok(buildTransferResponse(userId));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid amount format.");
        } catch (Exception e) {
            logger.error("Error processing transfer", e);
            return handleException(e);
        }
    }

    private void validateDepositRequest(Map<String, String> requestMap) {
        if (StringUtils.isEmpty(requestMap.get("deposit_amount")) || StringUtils.isEmpty(requestMap.get("account_id"))) {
            throw new IllegalArgumentException("Deposit amount and account ID cannot be empty.");
        }
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (StringUtils.isEmpty(request.getBeneficiary()) || StringUtils.isEmpty(request.getAccount_number())
                || StringUtils.isEmpty(request.getAccount_id()) || StringUtils.isEmpty(request.getPayment_amount())) {
            throw new IllegalArgumentException("Required fields cannot be empty.");
        }
    }

    private void validateWithdrawalRequest(Map<String, String> requestMap) {
        if (StringUtils.isEmpty(requestMap.get("withdrawal_amount")) || StringUtils.isEmpty(requestMap.get("account_id"))) {
            throw new IllegalArgumentException("Withdrawal amount and account ID cannot be empty.");
        }
    }

    private void validateTransferRequest(TransferRequest request) {
        if (StringUtils.isEmpty(request.getSourceAccount()) || StringUtils.isEmpty(request.getTargetAccount())
                || StringUtils.isEmpty(request.getAmount())) {
            throw new IllegalArgumentException("Transfer fields cannot be empty.");
        }
    }

    private void handleInsufficientFunds(int accountId) {
        transactRepository.logTransaction(accountId, "withdrawal", 0.0, "online", "failed", "Insufficient funds.",
                LocalDateTime.now());
    }

    // CRITICAL FIX (V-20): Generic error messages
    private ResponseEntity handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while processing your request.");
    }

    private Map<String, Object> buildDepositResponse(long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Amount Deposited Successfully.");
        response.put("accounts", accountRepository.getUserAccountsById(userId));
        return response;
    }

    private Map<String, Object> buildPaymentResponse(long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment Processed Successfully!");
        response.put("accounts", accountRepository.getUserAccountsById(userId));
        return response;
    }

    private Map<String, Object> buildWithdrawalResponse(long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Withdrawal Successful!");
        response.put("accounts", accountRepository.getUserAccountsById(userId));
        return response;
    }

    private Map<String, Object> buildTransferResponse(long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transfer Completed Successfully.");
        response.put("accounts", accountRepository.getUserAccountsById(userId));
        return response;
    }
}