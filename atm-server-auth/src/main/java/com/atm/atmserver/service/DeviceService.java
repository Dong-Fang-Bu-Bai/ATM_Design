package com.atm.atmserver.service;

import com.atm.atmserver.dto.CashCheckRequest;
import com.atm.atmserver.dto.CashCheckResponse;
import com.atm.atmserver.dto.DeviceStatusResponse;

import java.math.BigDecimal;

public interface DeviceService {
    DeviceStatusResponse getStatus();
    CashCheckResponse checkCashAvailability(CashCheckRequest request);
    void ensureCashAvailable(BigDecimal amount);
    void dispenseCash(BigDecimal amount);
}
