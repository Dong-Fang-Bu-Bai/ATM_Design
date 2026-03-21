package com.atm.atmserver.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户实体（对应 customer 表）
 */
@Data
public class Customer {
    private Long id;             // 客户ID（对应表的 id）
    private String customerName; // 客户姓名（对应表的 customer_name，下划线转驼峰）
    private String idCard;       // 身份证号（对应表的 id_card）
    private String phone;        // 手机号（对应表的 phone）
    private LocalDateTime createTime; // 开户时间（对应表的 create_time）
}