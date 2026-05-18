package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.Transaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionMapper {

    @Insert("INSERT INTO transaction_record (transaction_id, account_id, card_no, transaction_type, amount, " +
            "balance_before, balance_after, transaction_status, target_account_no, target_bank, " +
            "failure_reason, description, created_at) " +
            "VALUES (#{transactionId}, #{accountId}, #{cardNo}, #{transactionType}, #{amount}, " +
            "#{balanceBefore}, #{balanceAfter}, #{transactionStatus}, #{targetAccountNo}, #{targetBank}, " +
            "#{failureReason}, #{description}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Transaction transaction);

    @Update("UPDATE transaction_record SET transaction_status = #{status}, balance_after = #{balanceAfter}, " +
            "failure_reason = #{failureReason}, completed_at = #{completedAt} " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("balanceAfter") BigDecimal balanceAfter,
                     @Param("failureReason") String failureReason,
                     @Param("completedAt") LocalDateTime completedAt);

    @Select("SELECT * FROM transaction_record WHERE transaction_id = #{transactionId}")
    Transaction selectByTransactionId(String transactionId);

    @Select("SELECT * FROM transaction_record WHERE account_id = #{accountId} " +
            "ORDER BY created_at DESC, id DESC LIMIT #{limit} OFFSET #{offset}")
    List<Transaction> selectByAccountIdPaged(@Param("accountId") Long accountId,
                                             @Param("limit") int limit,
                                             @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM transaction_record WHERE account_id = #{accountId}")
    long countByAccountId(Long accountId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE card_no = #{cardNo} " +
            "AND transaction_type = #{type} AND transaction_status = 1 AND DATE(created_at) = CURDATE()")
    BigDecimal sumTodayAmount(@Param("cardNo") String cardNo, @Param("type") Integer type);
}
