package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.CashCheckRequest;
import com.atm.atmserver.dto.CashCheckResponse;
import com.atm.atmserver.dto.DeviceStatusResponse;
import com.atm.atmserver.service.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/status")
    public Result<DeviceStatusResponse> getStatus() {
        return Result.success(deviceService.getStatus());
    }

    @PostMapping("/cash-check")
    public Result<CashCheckResponse> checkCashAvailability(@RequestBody CashCheckRequest request) {
        return Result.success(deviceService.checkCashAvailability(request));
    }
}
