package com.example.bikerental.service;

import com.example.bikerental.model.BicycleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RentalCostCalculatorTest {

    private final RentalCostCalculator calculator = new RentalCostCalculator();

    @Test
    void chargesUrbanBikeRoundingActualUsageUpToNextHour() {
        RentalCost cost = calculator.calculate(BicycleType.URBANA, Duration.ofMinutes(70), 2);

        assertThat(cost.total()).isEqualByComparingTo(new BigDecimal("7000"));
        assertThat(cost.billedHours()).isEqualTo(2);
        assertThat(cost.fined()).isFalse();
    }

    @Test
    void doesNotRoundUpWhenUsageIsAnExactHour() {
        RentalCost cost = calculator.calculate(BicycleType.MONTANA, Duration.ofHours(2), 2);

        assertThat(cost.total()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(cost.billedHours()).isEqualTo(2);
        assertThat(cost.lateHours()).isZero();
    }

    @Test
    void appliesLateFineRoundingDelayUpToNextHour() {
        RentalCost cost = calculator.calculate(BicycleType.MONTANA, Duration.ofMinutes(200), 2);

        assertThat(cost.total()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(cost.billedHours()).isEqualTo(4);
        assertThat(cost.lateHours()).isEqualTo(2);
        assertThat(cost.fined()).isTrue();
    }
}
