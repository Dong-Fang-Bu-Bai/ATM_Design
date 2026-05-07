package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.Account;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface AccountMapper {

    @Select("SELECT * FROM account WHERE id = #{accountId}")
    Account selectById(Long accountId);

    @Select("SELECT * FROM account WHERE account_no = #{accountNo}")
    Account selectByAccountNo(String accountNo);

    @Update("UPDATE account SET balance = #{balance} WHERE id = #{accountId}")
    int updateBalance(Long accountId, BigDecimal balance);

    @Update("UPDATE account SET balance = balance + #{amount} WHERE id = #{accountId}")
    int addBalance(Long accountId, BigDecimal amount);

    @Update("UPDATE account SET balance = balance - #{amount} WHERE id = #{accountId} AND balance >= #{amount}")
    int subtractBalance(Long accountId, BigDecimal amount);
}
