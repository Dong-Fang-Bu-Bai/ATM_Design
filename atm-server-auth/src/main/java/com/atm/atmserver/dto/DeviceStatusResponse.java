package com.atm.atmserver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceStatusResponse {
    private String atmCode;
    private String location;
    private String status;
    private BigDecimal cashAvailable;
}
