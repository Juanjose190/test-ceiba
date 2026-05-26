package com.example.bikerental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 30)
    private String bicycleCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BicycleType bicycleType;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private int estimatedDurationHours;

    private LocalDateTime endTime;

    @Column(name = "actual_duration_seconds")
    private Duration actualDuration;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false)
    private boolean fined;

    protected Rental() {
    }

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
