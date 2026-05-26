package com.example.bikerental.service;

import java.math.BigDecimal;

public record RentalCost(
        BigDecimal total,
        boolean fined,
        long billedHours,
        long lateHours
) {
}
