package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.Transaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionMapper {

    @Insert("INSERT INTO transaction (transaction_no, account_id, card_no, transaction_type, amount, " +
            "balance_before, balance_after, transaction_status, target_account_no, target_bank, " +
            "failure_reason, description, created_at) " +
            "VALUES (#{transactionNo}, #{accountId}, #{cardNo}, #{transactionType}, #{amount}, " +
            "#{balanceBefore}, #{balanceAfter}, #{transactionStatus}, #{targetAccountNo}, #{targetBank}, " +
            "#{failureReason}, #{description}, #{createdAt})")
    int insert(Transaction transaction);

    @Update("UPDATE transaction SET transaction_status = #{status}, balance_after = #{balanceAfter}, " +
            "failure_reason = #{failureReason}, completed_at = #{completedAt} " +
            "WHERE id = #{id}")
    int updateStatus(Long id, Integer status, BigDecimal balanceAfter, String failureReason, LocalDateTime completedAt);

    @Select("SELECT * FROM transaction WHERE id = #{transactionId}")
    Transaction selectById(Long transactionId);

    @Select("SELECT * FROM transaction WHERE card_no = #{cardNo} AND transaction_type = #{type} " +
            "AND DATE(created_at) = CURDATE() ORDER BY created_at DESC")
    List<Transaction> selectTodayTransactions(String cardNo, Integer type);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction WHERE card_no = #{cardNo} " +
            "AND transaction_type = #{type} AND transaction_status = 1 AND DATE(created_at) = CURDATE()")
    BigDecimal sumTodayAmount(String cardNo, Integer type);
}
