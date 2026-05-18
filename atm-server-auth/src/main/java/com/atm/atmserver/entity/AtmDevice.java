package com.atm.atmserver.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AtmDevice {
    private Long id;
    private String atmCode;
    private String location;
    private String status;
    private BigDecimal cashAvailable;
    private LocalDateTime updatedAt;

    public static final String STATUS_RUNNING = "RUNNING";
}
