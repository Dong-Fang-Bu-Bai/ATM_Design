package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.BankCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BankCardMapper {

    @Select("SELECT * FROM bank_card WHERE card_no = #{cardNo}")
    BankCard selectByCardNo(String cardNo);

    /**
     * 更新银行卡密码
     * @param cardNo 卡号
     * @param newPassword 新密码
     * @return 影响的行数
     */
    @Update("UPDATE bank_card SET password = #{newPassword} WHERE card_no = #{cardNo}")
    int updatePassword(String cardNo, String newPassword);
}