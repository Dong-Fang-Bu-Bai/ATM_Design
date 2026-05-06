package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.BankCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BankCardMapper {

    @Select("SELECT * FROM bank_card WHERE card_no = #{cardNo}")
    BankCard selectByCardNo(String cardNo);

    @Update("UPDATE bank_card SET password = #{newPassword} WHERE card_no = #{cardNo}")
    int updatePassword(@Param("cardNo") String cardNo, @Param("newPassword") String newPassword);
}
