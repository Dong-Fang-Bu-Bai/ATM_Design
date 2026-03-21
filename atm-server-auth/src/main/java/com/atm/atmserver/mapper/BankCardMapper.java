package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.BankCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BankCardMapper {

    @Select("SELECT * FROM bank_card WHERE card_no = #{cardNo}")
    BankCard selectByCardNo(String cardNo);
}