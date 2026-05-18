package com.atm.atmserver.service.impl;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.CashCheckRequest;
import com.atm.atmserver.dto.CashCheckResponse;
import com.atm.atmserver.dto.DeviceStatusResponse;
import com.atm.atmserver.entity.AtmDevice;
import com.atm.atmserver.mapper.AtmDeviceMapper;
import com.atm.atmserver.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final AtmDeviceMapper atmDeviceMapper;

    public DeviceServiceImpl(AtmDeviceMapper atmDeviceMapper) {
        this.atmDeviceMapper = atmDeviceMapper;
    }

    @Override
    public DeviceStatusResponse getStatus() {
        AtmDevice device = loadDevice();
        DeviceStatusResponse response = new DeviceStatusResponse();
        response.setAtmCode(device.getAtmCode());
        response.setLocation(device.getLocation());
        response.setStatus(device.getStatus());
        response.setCashAvailable(device.getCashAvailable());
        return response;
    }

    @Override
    public CashCheckResponse checkCashAvailability(CashCheckRequest request) {
        BigDecimal amount = requirePositiveAmount(request == null ? null : request.getAmount());
        AtmDevice device = loadDevice();

        CashCheckResponse response = new CashCheckResponse();
        response.setAmount(amount);
        response.setCashAvailable(device.getCashAvailable());
        response.setAvailable(isRunning(device) && device.getCashAvailable().compareTo(amount) >= 0);
        return response;
    }

    @Override
    public void ensureCashAvailable(BigDecimal amount) {
        BigDecimal checkedAmount = requirePositiveAmount(amount);
        AtmDevice device = loadDevice();
        if (!isRunning(device)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ATM设备当前不可用");
        }
        if (device.getCashAvailable().compareTo(checkedAmount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ATM现金不足");
        }
    }

    @Override
    public void dispenseCash(BigDecimal amount) {
        BigDecimal checkedAmount = requirePositiveAmount(amount);
        AtmDevice device = loadDevice();
        if (!isRunning(device)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ATM设备当前不可用");
        }
        int updateCount = atmDeviceMapper.subtractCash(device.getId(), checkedAmount);
        if (updateCount == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ATM现金不足");
        }
    }

    private AtmDevice loadDevice() {
        AtmDevice device = atmDeviceMapper.selectPrimaryDevice();
        if (device == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ATM设备不存在");
        }
        return device;
    }

    private boolean isRunning(AtmDevice device) {
        return AtmDevice.STATUS_RUNNING.equals(device.getStatus());
    }

    private BigDecimal requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "无效金额");
        }
        return amount;
    }
}
