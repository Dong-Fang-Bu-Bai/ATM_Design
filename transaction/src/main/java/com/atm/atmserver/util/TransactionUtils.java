package com.atm.atmserver.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TransactionUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String generateTransactionNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "TXN" + timestamp + uuid;
    }

    public static boolean isMultipleOfHundred(Long amount) {
        return amount != null && amount % 100 == 0;
    }
}
