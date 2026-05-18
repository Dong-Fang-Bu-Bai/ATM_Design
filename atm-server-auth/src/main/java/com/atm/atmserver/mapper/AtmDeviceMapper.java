package com.atm.atmserver.mapper;

import com.atm.atmserver.entity.AtmDevice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface AtmDeviceMapper {

    @Select("SELECT * FROM atm_device ORDER BY id LIMIT 1")
    AtmDevice selectPrimaryDevice();

    @Update("UPDATE atm_device SET cash_available = cash_available - #{amount}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{deviceId} AND status = 'RUNNING' AND cash_available >= #{amount}")
    int subtractCash(@Param("deviceId") Long deviceId, @Param("amount") BigDecimal amount);
}
