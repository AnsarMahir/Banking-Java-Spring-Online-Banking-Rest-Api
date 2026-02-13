package com.beko.DemoBank_v1.repository;

import com.beko.DemoBank_v1.models.TransactionHistory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactHistoryRepository extends CrudRepository<TransactionHistory, Integer> {

    @Query(value = "SELECT t.* FROM transaction_history t JOIN accounts a ON t.account_id = a.account_id WHERE a.user_id = :user_id ORDER BY t.transaction_id DESC", nativeQuery = true)
    List<TransactionHistory> getTransactionRecordsById(@Param("user_id") long user_id);

    @Query(value = "SELECT * FROM transaction_history WHERE account_id = :account_id", nativeQuery = true)
    List<TransactionHistory> getTransactionRecordsByAccountId(@Param("account_id") int user_id);

}
