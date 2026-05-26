package com.example.bikerental.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Rental {

    private final UUID id;
    private final String bicycleCode;
    private final BicycleType bicycleType;
    private final String customerName;
    private final LocalDateTime startTime;
    private final int estimatedDurationHours;
    private LocalDateTime endTime;
    private Duration actualDuration;
    private BigDecimal totalCost;
    private boolean fined;

    public Rental(UUID id, String bicycleCode, BicycleType bicycleType, String customerName,
                  LocalDateTime startTime, int estimatedDurationHours) {
        this.id = Objects.requireNonNull(id);
        this.bicycleCode = Objects.requireNonNull(bicycleCode);
        this.bicycleType = Objects.requireNonNull(bicycleType);
        this.customerName = Objects.requireNonNull(customerName);
        this.startTime = Objects.requireNonNull(startTime);
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public UUID getId() {
        return id;
    }

    public String getBicycleCode() {
        return bicycleCode;
    }

    public BicycleType getBicycleType() {
        return bicycleType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Duration getActualDuration() {
        return actualDuration;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public boolean isFined() {
        return fined;
    }

    public boolean isFinished() {
        return endTime != null;
    }

    public void finish(LocalDateTime endTime, Duration actualDuration, BigDecimal totalCost, boolean fined) {
        this.endTime = Objects.requireNonNull(endTime);
        this.actualDuration = Objects.requireNonNull(actualDuration);
        this.totalCost = Objects.requireNonNull(totalCost);
        this.fined = fined;
    }
}
