package com.example.bikerental.service;

import com.example.bikerental.model.BicycleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class RentalCostCalculator {

    private static final BigDecimal FINE_RATE = new BigDecimal("0.50");

    public RentalCost calculate(BicycleType bicycleType, Duration actualDuration, int estimatedDurationHours) {
        if (actualDuration.isNegative() || actualDuration.isZero()) {
            throw new IllegalArgumentException("La duración real debe ser mayor a cero");
        }

        long billedHours = roundUpToHours(actualDuration);
        BigDecimal baseCost = bicycleType.getHourlyRate().multiply(BigDecimal.valueOf(billedHours));

        Duration estimatedDuration = Duration.ofHours(estimatedDurationHours);
        Duration delay = actualDuration.minus(estimatedDuration);
        if (delay.compareTo(Duration.ZERO) <= 0) {
            return new RentalCost(baseCost, false, billedHours, 0);
        }

        long lateHours = roundUpToHours(delay);
        BigDecimal fine = bicycleType.getHourlyRate()
                .multiply(FINE_RATE)
                .multiply(BigDecimal.valueOf(lateHours));

        return new RentalCost(baseCost.add(fine), true, billedHours, lateHours);
    }

    private long roundUpToHours(Duration duration) {
        long seconds = duration.getSeconds();
        if (duration.getNano() > 0) {
            seconds++;
        }
        return Math.max(1, (seconds + 3599) / 3600);
    }
}
