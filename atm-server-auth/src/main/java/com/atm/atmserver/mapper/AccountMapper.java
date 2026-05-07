package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.Account;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * 账户数据访问层（对接 account 表）
 */
@Repository
public interface AccountMapper {
    /**
     * 根据账户ID查询账户信息
     * @param accountId 账户ID
     * @return 账户实体
     */
    @Select("SELECT * FROM account WHERE id = #{accountId}")
    Account selectById(Long accountId);

    @Select("SELECT * FROM account WHERE account_no = #{accountNo}")
    Account selectByAccountNo(String accountNo);

    @Update("UPDATE account SET balance = balance + #{amount} WHERE id = #{accountId}")
    int addBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    @Update("UPDATE account SET balance = balance - #{amount} WHERE id = #{accountId} AND balance >= #{amount}")
    int subtractBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);
}
