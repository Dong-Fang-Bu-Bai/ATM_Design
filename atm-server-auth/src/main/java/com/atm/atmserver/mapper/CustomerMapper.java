package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.Customer;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 客户数据访问层（对接 customer 表）
 */
@Repository // 标记为持久层组件，避免 IDEA 注入警告
public interface CustomerMapper {
    /**
     * 根据客户ID查询客户信息
     * @param customerId 客户ID
     * @return 客户实体
     */
    @Select("SELECT * FROM customer WHERE id = #{customerId}")
    Customer selectById(Long customerId);
}