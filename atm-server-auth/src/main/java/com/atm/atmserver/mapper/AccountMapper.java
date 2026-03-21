package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.Account;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

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
}