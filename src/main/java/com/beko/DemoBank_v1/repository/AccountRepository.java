package com.beko.DemoBank_v1.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.beko.DemoBank_v1.models.Account;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<Account, Integer> {

    @Query(value = "SELECT * FROM accounts WHERE user_id = :user_id", nativeQuery = true)
    List<Account> getUserAccountsById(@Param("user_id") long user_id);

    @Query(value = "SELECT sum(balance) FROM accounts WHERE user_id = :user_id", nativeQuery = true)
    BigDecimal getTotalBalance(@Param("user_id") long user_id);

        @Query(value = "SELECT COALESCE(balance, 0) FROM accounts WHERE user_id = :user_id AND account_id = :account_id", nativeQuery = true)
        double getAccountBalance(@Param("user_id") long user_id, @Param("account_id") int account_id);

    // CRITICAL FIX (V-03, V-04, V-05): Add ownership verification method
    @Query(value = "SELECT COUNT(*) > 0 FROM accounts WHERE user_id = :user_id AND account_id = :account_id", nativeQuery = true)
    boolean isAccountOwnedByUser(@Param("user_id") long user_id, @Param("account_id") int account_id);

    // CRITICAL FIX (V-19): Update to use BigDecimal instead of double
    @Modifying
    @Query(value = "UPDATE accounts SET balance = :new_balance WHERE account_id = :account_id", nativeQuery = true)
    @Transactional
    void changeAccountsBalanceById(@Param("new_balance") BigDecimal new_balance, @Param("account_id") int account_id);

        @Modifying
        @Query(value = "INSERT INTO accounts(user_id, account_number, account_name, account_type, balance) VALUES" +
                        "(:user_id, :account_number, :account_name, :account_type, 0)", nativeQuery = true)

        @Transactional
        void createBankAccount(@Param("user_id") long user_id,
                        @Param("account_number") String account_number,
                        @Param("account_name") String account_name,
                        @Param("account_type") String account_type);
}